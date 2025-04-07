package io.jenkins.plugins.extended_timer_trigger;

import hudson.scheduler.CronTabList;
import hudson.scheduler.Hash;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.accmod.restrictions.suppressions.SuppressRestrictedWarnings;

@Restricted(NoExternalUse.class)
public class ExtendedCronTabList {

  private static final Logger LOGGER = Logger.getLogger(ExtendedCronTabList.class.getName());

  private final transient List<CronTabWrapper> cronTabWrapperList = new ArrayList<>();

  private ExtendedCronTabList() {
  }

  public static ExtendedCronTabList create(String spec, Hash hash) {
    ExtendedCronTabList ectl = new ExtendedCronTabList();
    ectl.load(spec, hash);
    return ectl;
  }

  List<CronTabWrapper> getCronTabWrapperList() {
    return cronTabWrapperList;
  }

  public String checkSanity() {
    for (CronTabWrapper ctl: cronTabWrapperList) {
      String s = ctl.checkSanity();
      if (s != null) {
        return s;
      }
    }
    return null;
  }

  @SuppressRestrictedWarnings(CronTabList.class)
  public ZonedDateTime previous() {
    ZonedDateTime previous = null;
    for (CronTabWrapper wrapper: cronTabWrapperList) {
      ZonedDateTime scheduled = wrapper.previous();
      if (previous == null || (scheduled != null && previous.isBefore(scheduled))) {
        previous = scheduled;
      }
    }
    return previous;
  }

  @SuppressRestrictedWarnings(CronTabList.class)
  public ZonedDateTime next() {
    ZonedDateTime next = null;
    for (CronTabWrapper wrapper: cronTabWrapperList) {
      ZonedDateTime scheduled = wrapper.next();
      if (next == null || (scheduled != null && next.isAfter(scheduled))) {
        next = scheduled;
      }
    }
    return next;
  }

  private void load(String cronSpec, Hash hash) {
    int lineNumber = 0;
    String timezone = "";
    ZoneId timeZoneId = null;
    boolean isParamLine = false;
    String currentParameterName = null;
    StringBuilder currentParameterValue = null;
    CronTabWrapper currentCronTab = null;
    Map<String, String> currentParameters = null;

    if (cronSpec != null) {
      for (String line : cronSpec.split("\\r?\\n")) {
        lineNumber++;
        line = line.trim();

        LOGGER.log(Level.FINE, "Reading line {0}: {1}", new Object[]{lineNumber, line});

        if (!line.startsWith("%") && line.contains("//")) {
          line = line.substring(0, line.indexOf("//")).trim();
        }

        if (line.isEmpty() || line.startsWith("#")) {
          continue;
        }

        if (line.startsWith("%")) {
          String paramLine = line.substring(1);
          if (!isParamLine) {
            currentParameters = new HashMap<>();
          }
          if (paramLine.startsWith("%")) {
            if (currentParameterValue == null) {
              LOGGER.log(Level.FINER, "No current parameter for multiline value defined at line {0}: {1}", new Object[] { lineNumber, line });
              continue;
            }
            currentParameterValue.append("\n").append(paramLine.substring(1));
          } else {
            if (currentParameterName != null) {
              currentParameters.put(currentParameterName, currentParameterValue.toString());
            }
            String[] param = paramLine.split("=", 2);
            if (param.length != 2) {
              currentParameterName = null;
              currentParameterValue = null;
              LOGGER.log(Level.FINER, "No Parameter name found at line {0}: {1}", new Object[] { lineNumber, line });
              continue;
            }
            currentParameterName = param[0];
            currentParameterValue = new StringBuilder();
            currentParameterValue.append(param[1]);
          }
          isParamLine = true;
          continue;
        } else {
          if (currentParameters != null && currentParameterName != null) {
            currentParameters.put(currentParameterName, currentParameterValue.toString());
          }
          if (currentCronTab != null && currentParameters != null) {
            currentCronTab.setParameters(currentParameters);
          }
          currentCronTab = null;
          currentParameterName = null;
          currentParameterValue = null;
          currentParameters = null;
          isParamLine = false;
        }

        if (line.startsWith("TZ=")) {
          try {
            timezone = line.replace("TZ=", "");
            LOGGER.log(Level.FINER, "Found timezone: {0}", timezone);
            if (timezone.isEmpty()) {
              timeZoneId = null;
              timezone = "";
            } else {
              timeZoneId = ZoneId.of(line.substring(3));
              timezone = line;
            }
            continue;
          } catch (DateTimeException e) {
            LOGGER.log(Level.WARNING, "Failed to parse timezone at line {0}: {1}", new Object[] { lineNumber, line });
            throw new IllegalArgumentException(e);
          }
        }

        try {
          String cronTabLine = timezone + "\n" + line;
          CronTabList cronTab = CronTabList.create(cronTabLine, hash);
          currentCronTab = new CronTabWrapper(cronTab);
          LOGGER.log(Level.FINER, "Crontab line {0} has Jenkins syntax: {1}", new Object[]{lineNumber, line});
        } catch (IllegalArgumentException e) {
          LOGGER.log(Level.FINER, "Crontab line {0} is not a Jenkins syntax crontab: {1}", new Object[]{lineNumber, line});
          LOGGER.log(Level.FINEST, "Error", e);
          currentCronTab = new CronTabWrapper(new ExtendedCronTab(line, timeZoneId, hash));
        }
        cronTabWrapperList.add(currentCronTab);
      }
      if (currentCronTab != null && currentParameters != null && currentParameterName != null && currentParameterValue != null) {
        currentParameters.put(currentParameterName, currentParameterValue.toString());
        currentCronTab.setParameters(currentParameters);
      }
    }
  }
}
