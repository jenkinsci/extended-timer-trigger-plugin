package io.jenkins.plugins.extended_timer_trigger;

import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import hudson.scheduler.Hash;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

  private static final int[] LOWER_BOUNDS = new int[] {0, 0, 1, 1, 0};
  private static final int[] UPPER_BOUNDS = new int[] {59, 23, 28, 12, 6};
  private static final String[] FIELD_NAMES = new String[] {"Minute", "Hour", "Day-of-Month", "Month", "Day-of-Week"};

  private static final Pattern rangePattern = Pattern.compile("^H\\((\\d+)-(\\d+)\\)(/(-?\\d+))?$");
  private static final Pattern stepPattern = Pattern.compile("^H/(\\d+)$");

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
    String[] tokens = spec.split(" +");
    if (tokens.length != 5) {
      return spec;
    }
    processHash(tokens, 0, hash);
    processHash(tokens, 1, hash);
    processHash(tokens, 2, hash);
    processHash(tokens, 3, hash);
    processHash(tokens, 4, hash);
    return String.join(" ", tokens);
  }

  private void processHash(String[] tokens, int field, Hash hash) {
    int lowerBound = LOWER_BOUNDS[field];
    int upperBound = UPPER_BOUNDS[field];
    if (tokens[field].equals("H")) {
      tokens[field] = String.valueOf(hash.next(upperBound + 1 - lowerBound) + lowerBound);
      return;
    }
    Matcher m = rangePattern.matcher(tokens[field]);
    if (m.matches()) {
      int lower = Integer.parseInt(m.group(1));
      int upper = Integer.parseInt(m.group(2));
      int step = 1;
      String stepStr = m.group(4);
      if (stepStr != null) {
        step = Integer.parseInt(m.group(4));
      }
      String unhashed = hashToString(lower, upper, step, field, hash);
      tokens[field] = unhashed;
      return;
    }
    m = stepPattern.matcher(tokens[field]);
    if (m.matches()) {
      int step = Integer.parseInt(m.group(1));
      String unhashed = hashToString(lowerBound, upperBound, step, field, hash);
      tokens[field] = unhashed;
    }
  }

  private String hashToString(int lower, int upper, int step, int field, Hash hash) {
    rangeCheck(lower, field);
    rangeCheck(upper, field);
    StringBuilder builder = new StringBuilder();
    if (lower > upper) {
      throw new IllegalArgumentException(Messages.ExtendedCronTab_LowerUpper(lower, upper));
    }
    if (step > upper - lower + 1) {
      throw new IllegalArgumentException(Messages.ExtendedCronTab_OutOfRange(step, 1, upper - lower + 1, FIELD_NAMES[field]));
    } else if (step > 1) {
      for (int i = hash.next(step) + lower; i <= upper; i += step) {
        if (!builder.isEmpty()) {
          builder.append(",");
        }
        builder.append(i);
      }
      return builder.toString();
    } else if (step <= 0) {
      throw new IllegalArgumentException(Messages.ExtendedCronTab_MustBePositive(step));
    } else {
      // step=1 (i.e. omitted) in the case of hash is actually special; means pick one value, not step by 1
      return Integer.toString(lower + hash.next(upper + 1 - lower));
    }
  }

  protected void rangeCheck(int value, int field) {
    if (value < LOWER_BOUNDS[field] || UPPER_BOUNDS[field] < value) {
      throw new IllegalArgumentException(Messages.ExtendedCronTab_OutOfRange(value, LOWER_BOUNDS[field], UPPER_BOUNDS[field], FIELD_NAMES[field]));
    }
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

  public ZonedDateTime ceil(long timeInMillis) {
    Instant instant = Instant.ofEpochMilli(timeInMillis);
    ZonedDateTime time = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
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

  public ZonedDateTime floor(long timeInMillis) {
    Instant instant = Instant.ofEpochMilli(timeInMillis);
    ZonedDateTime time = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
    if (zoneId != null) {
      time = time.withZoneSameInstant(zoneId);
    }
    return ExecutionTime.forCron(cron).lastExecution(time).orElse(null);
  }
}
