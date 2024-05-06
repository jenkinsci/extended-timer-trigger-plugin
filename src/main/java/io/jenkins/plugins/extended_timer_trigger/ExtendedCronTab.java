package io.jenkins.plugins.extended_timer_trigger;

import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

@Restricted(NoExternalUse.class)
public class ExtendedCronTab {

  private static final CronDefinition cronDefinition = CronDefinitionBuilder.defineCron()
      .withMinutes().withValidRange(0, 59).withStrictRange().and()
      .withHours().withValidRange(0, 23).withStrictRange().and()
      .withDayOfMonth().withValidRange(1, 31).supportsL().supportsW().supportsLW().supportsQuestionMark().and()
      .withMonth().withValidRange(1, 12).withStrictRange().and()
      .withDayOfWeek().withValidRange(0, 7).withMondayDoWValue(1).withIntMapping(7, 0).supportsHash()
      .supportsL().supportsQuestionMark().withStrictRange().and().instance();

  private static final CronParser parser = new CronParser(cronDefinition);

  private final Cron cron;
  private final ZoneId zoneId;

  public ExtendedCronTab(String spec, ZoneId zoneId) {
    this.cron = parser.parse(spec);
    this.zoneId = zoneId;
  }

  public boolean check(ZonedDateTime time) {
    if (zoneId != null) {
      time = time.withZoneSameInstant(zoneId);
    }
    return ExecutionTime.forCron(cron).isMatch(time);
  }

  public ZonedDateTime next() {
    ZonedDateTime time = ZonedDateTime.now();
    if (zoneId != null) {
      time = time.withZoneSameInstant(zoneId);
    }
    return ExecutionTime.forCron(cron).nextExecution(time).orElse(null);
  }

  public ZonedDateTime previous() {
    ZonedDateTime time = ZonedDateTime.now();
    if (zoneId != null) {
      time = time.withZoneSameInstant(zoneId);
    }
    return ExecutionTime.forCron(cron).lastExecution(time).orElse(null);
  }
}
