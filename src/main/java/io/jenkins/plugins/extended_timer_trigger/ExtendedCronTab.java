package io.jenkins.plugins.extended_timer_trigger;

import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import hudson.scheduler.Hash;
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

  public ExtendedCronTab(String spec, ZoneId zoneId, Hash hash) {
    this.cron = parser.parse(translateHash(spec, hash));
    this.zoneId = zoneId;
  }

  private String translateHash(String spec, Hash hash) {
    if (hash == null) {
      hash = Hash.zero();
    }
    String[] tokens = spec.split(" ");
    if (tokens.length != 5) {
      return spec;
    }
    if (tokens[0].equals("H")) {
      tokens[0] = "" + hash.next(60);
    }
    if (tokens[1].equals("H")) {
      tokens[1] = "" + hash.next(24);
    }
    if (tokens[3].equals("H")) {
      tokens[3] = "" + hash.next(12) + 1;
    }
    if (tokens[4].equals("H")) {
      tokens[4] = "" + hash.next(7);
    }
    return String.join(" ", tokens);
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
