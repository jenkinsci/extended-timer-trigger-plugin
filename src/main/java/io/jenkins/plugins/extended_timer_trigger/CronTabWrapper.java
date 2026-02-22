package io.jenkins.plugins.extended_timer_trigger;

import com.google.common.annotations.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.scheduler.CronTab;
import hudson.scheduler.CronTabList;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;
import org.kohsuke.accmod.restrictions.suppressions.SuppressRestrictedWarnings;

public class CronTabWrapper {

  private final CronTabList cronTabList;
  private final CronTab cronTab;
  private final ExtendedCronTab extendedCronTab;
  private Map<String, String> parameters;

  public CronTabWrapper(CronTabList cronTabList, CronTab cronTab) {
    this.cronTabList = cronTabList;
    this.extendedCronTab = null;
    this.cronTab = cronTab;
  }

  public CronTabWrapper(ExtendedCronTab extendedCronTab) {
    this.extendedCronTab = extendedCronTab;
    this.cronTabList = null;
    this.cronTab = null;
  }

  @VisibleForTesting
  CronTab getCronTab() {
    return cronTab;
  }

  public void setParameters(Map<String, String> parameters) {
    this.parameters = parameters;
  }

  @CheckForNull
  public Map<String, String> getParameters() {
    return parameters;
  }

  public boolean check(ZonedDateTime time) {
    if (cronTabList != null) {
      Calendar cal = GregorianCalendar.from(time);
      return cronTabList.check(cal);
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
    if (cronTabList != null) {
      Calendar scheduled = cronTabList.previous();
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

  @CheckForNull
  @SuppressRestrictedWarnings(CronTabList.class)
  public ZonedDateTime next() {
    if (cronTabList != null) {
      Calendar scheduled = cronTabList.next();
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
  public ZonedDateTime ceil(long timeInMillis) {
    if (cronTab != null) {
      Calendar scheduled = cronTab.ceil(timeInMillis);
      return ZonedDateTime.ofInstant(scheduled.toInstant(), scheduled.getTimeZone().toZoneId());
    } else {
      if (extendedCronTab != null) {
        return extendedCronTab.ceil(timeInMillis);
      }
    }
    return null;
  }

  @CheckForNull
  public ZonedDateTime floor(long timeInMillis) {
    if (cronTab != null) {
      Calendar scheduled = cronTab.floor(timeInMillis);
      return ZonedDateTime.ofInstant(scheduled.toInstant(), scheduled.getTimeZone().toZoneId());
    } else {
      if (extendedCronTab != null) {
        return extendedCronTab.floor(timeInMillis);
      }
    }
    return null;
  }

  @CheckForNull
  public String checkSanity() {
    if (cronTabList != null) {
      return cronTabList.checkSanity();
    }
    return null;
  }
}
