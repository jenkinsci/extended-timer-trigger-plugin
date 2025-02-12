package io.jenkins.plugins.extended_timer_trigger;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.scheduler.CronTabList;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.accmod.restrictions.suppressions.SuppressRestrictedWarnings;

@Restricted(NoExternalUse.class)
public class CronTabWrapper {

  private final CronTabList cronTab;
  private final ExtendedCronTab extendedCronTab;
  private Map<String, String> parameters;

  public CronTabWrapper(CronTabList cronTab) {
    this.cronTab = cronTab;
    this.extendedCronTab = null;
  }

  public CronTabWrapper(ExtendedCronTab extendedCronTab) {
    this.extendedCronTab = extendedCronTab;
    this.cronTab = null;
  }

  public void setParameters(Map<String, String> parameters) {
    this.parameters = parameters;
  }

  @CheckForNull
  public Map<String, String> getParameters() {
    return parameters;
  }

  public boolean check(ZonedDateTime time) {
    if (cronTab != null) {
      Calendar cal = GregorianCalendar.from(time);
      return cronTab.check(cal);
    } else {
      if (extendedCronTab != null) {
        return extendedCronTab.check(time);
      }
    }
    return false;
  }

  @CheckForNull
  @SuppressRestrictedWarnings(CronTabList.class)
  public ZonedDateTime previous() {
    if (cronTab != null) {
      Calendar scheduled = cronTab.previous();
      if (scheduled != null) {
        return ZonedDateTime.ofInstant(scheduled.toInstant(), scheduled.getTimeZone().toZoneId());
      }
    } else {
      if (extendedCronTab != null) {
        return extendedCronTab.previous();
      }
    }
    return null;
  }

  @SuppressRestrictedWarnings(CronTabList.class)
  public ZonedDateTime next() {
    if (cronTab != null) {
      Calendar scheduled = cronTab.next();
      if (scheduled != null) {
        return ZonedDateTime.ofInstant(scheduled.toInstant(), scheduled.getTimeZone().toZoneId());
      }
    } else {
      if (extendedCronTab != null) {
        return extendedCronTab.next();
      }
    }
    return null;
  }

  @CheckForNull
  public String checkSanity() {
    if (cronTab != null) {
      return cronTab.checkSanity();
    }
    return null;
  }
}
