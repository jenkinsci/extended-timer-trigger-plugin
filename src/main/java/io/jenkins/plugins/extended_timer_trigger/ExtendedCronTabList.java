package io.jenkins.plugins.extended_timer_trigger;

import hudson.scheduler.CronTab;
import hudson.scheduler.CronTabList;
import hudson.scheduler.Hash;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.accmod.restrictions.suppressions.SuppressRestrictedWarnings;

@Restricted(NoExternalUse.class)
public class ExtendedCronTabList {

  private static final Logger LOGGER = Logger.getLogger(ExtendedCronTabList.class.getName());

  private final transient List<ExtendedCronTab> cronList = new ArrayList<>();
  private final transient List<CronTabList> cronTabLists = new ArrayList<>();

  private ExtendedCronTabList() {
  }

  public static ExtendedCronTabList create(String spec, Hash hash) {
    ExtendedCronTabList ectl = new ExtendedCronTabList();
    ectl.load(spec, hash);
    return ectl;
  }

  public boolean check(ZonedDateTime time) {
    boolean check = cronList.stream().anyMatch(it -> it.check(time));
    if (!check) {
      Calendar cal = GregorianCalendar.from(time);
      check = cronTabLists.stream().anyMatch(it -> it.check(cal));
    }
    return check;
  }

  public String checkSanity() {
    for (CronTabList ctl: cronTabLists) {
      String s = ctl.checkSanity();
      if (s != null) {
        return s;
      }
    }
    return null;
  }

  @SuppressRestrictedWarnings(CronTabList.class)
  public ZonedDateTime previous() {
    Calendar nearest = null;
    for (CronTabList ctl: cronTabLists) {
      Calendar scheduled = ctl.previous();
      if (nearest == null || nearest.before(scheduled)) {
        nearest = scheduled;
      }
    }
    ZonedDateTime previous = null;
    if (nearest != null) {
      previous = ZonedDateTime.ofInstant(nearest.toInstant(), ZoneId.systemDefault());
    }
    for (ExtendedCronTab ect: cronList) {
      ZonedDateTime scheduled = ect.previous();
      if (previous == null || (scheduled != null && previous.isBefore(scheduled))) {
        previous = scheduled;
      }
    }
    return previous;
  }

  @SuppressRestrictedWarnings(CronTabList.class)
  public ZonedDateTime next() {
    Calendar nearest = null;
    for (CronTabList ctl: cronTabLists) {
      Calendar scheduled = ctl.next();
      if (nearest == null || nearest.after(scheduled)) {
        nearest = scheduled;
      }
    }
    ZonedDateTime next = null;
    if (nearest != null) {
      next = ZonedDateTime.ofInstant(nearest.toInstant(), ZoneId.systemDefault());
    }
    for (ExtendedCronTab ect: cronList) {
      ZonedDateTime scheduled = ect.next();
      if (next == null || (scheduled != null && next.isAfter(scheduled))) {
        next = scheduled;
      }
    }
    return next;
  }

  private void load(String cronSpec, Hash hash) {
    int lineNumber = 0;
    String timezone = null;
    String oldTimezone = "";
    String timezoneLine = "";
    ZoneId timeZoneId = null;
    List<String> cronTabListLines = new ArrayList<>();

    if (cronSpec != null) {
      for (String line : cronSpec.split("\n")) {
        lineNumber++;
        line = line.trim();

        LOGGER.log(Level.FINE, "Reading line", new Object[]{lineNumber, line});

        if (line.contains("//")) {
          line = line.substring(0, line.indexOf("//")).trim();
        }

        if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) {
          continue;
        }

        if (line.startsWith("TZ=")) {
          try {
            timezone = line.replace("TZ=", "");
            LOGGER.log(Level.FINER, "Found timezone: {0}", timezone);
            if (!oldTimezone.equals(timezone) && !cronTabListLines.isEmpty()) {
              LOGGER.log(Level.FINER, "Saving {0} Jenkins crons for timezone: {1}", new Object[] {cronTabListLines.size(), oldTimezone});
              if (!oldTimezone.isEmpty()) {
                cronTabListLines.add(0,timezoneLine );
              }
              cronTabLists.add(CronTabList.create(String.join("\n",cronTabListLines)));
              cronTabListLines.clear();
            }
            if (timezone.isEmpty()) {
              timeZoneId = null;
              timezoneLine = "";
            } else {
              timeZoneId = ZoneId.of(line.substring(3));
              timezoneLine = line;
            }
            oldTimezone = timezone;
            continue;
          } catch (DateTimeException e) {
            LOGGER.log(Level.WARNING, "Failed to parse timezone at line {0}: {1}", new Object[] { lineNumber, line });
            throw new IllegalArgumentException(e);
          }
        }

        try {
          new CronTab(line, lineNumber, hash, timezone);
          cronTabListLines.add(line);
          LOGGER.log(Level.FINER, "Crontab line {0} has Jenkins syntax: {1}", new Object[]{lineNumber, line});
        } catch (IllegalArgumentException e) {
          LOGGER.log(Level.FINER, "Crontab line {0} is not a Jenkins syntax crontab: {1}", new Object[]{lineNumber, line});
          LOGGER.log(Level.FINEST, "Error", e);
          cronList.add(new ExtendedCronTab(line, timeZoneId, hash));
        }
      }
      if (!cronTabListLines.isEmpty()) {
        if (!timezoneLine.isEmpty()) {
          cronTabListLines.add(0,timezoneLine );
        }
        LOGGER.log(Level.FINER, "Adding remaining Jenkins syntax crontab ({0} lines), {1}", new Object[] {cronTabListLines.size(), timezoneLine});
        cronTabLists.add(CronTabList.create(String.join("\n",cronTabListLines)));
      }
    }
  }
}
