package io.jenkins.plugins.extended_timer_trigger;

import static hudson.Util.fixNull;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.BuildableItem;
import hudson.model.Item;
import hudson.model.PeriodicWork;
import hudson.scheduler.Hash;
import hudson.triggers.TimerTrigger;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import hudson.util.FormValidation;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import jenkins.triggers.TriggeredItem;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.verb.POST;

/**
 * Trigger that runs a job periodically.
 */
@Restricted(NoExternalUse.class)
public class ExtendedTimerTrigger extends Trigger<BuildableItem> {

  private static final Logger LOGGER = Logger.getLogger(ExtendedTimerTrigger.class.getName());

  private transient ExtendedCronTabList extendedCronTabList;

  private final String cronSpec;
  @DataBoundConstructor
  public ExtendedTimerTrigger(@NonNull String cronSpec) {
    this.cronSpec = cronSpec;
    this.extendedCronTabList = ExtendedCronTabList.create(cronSpec, null);
  }

  public String getCronSpec() {
    return cronSpec;
  }

  @Override
  public void start(BuildableItem project, boolean newInstance) {
    this.job = project;
    Hash hash = Hash.from(project.getFullName());

    if (cronSpec != null) {
      try {
        this.extendedCronTabList = ExtendedCronTabList.create(cronSpec, hash);
      } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
        LOGGER.log(Level.WARNING, String.format("Failed to parse crontab spec %s in job %s", cronSpec, project.getFullName()), e);
      }
    }
  }

  @Override
  public void run() {
    if (job == null) {
      return;
    }

    job.scheduleBuild(0, new ExtendenTimerTriggerCause());
  }


  @Extension
  @Symbol("extendedCron")
  public static class DescriptorImpl extends TriggerDescriptor {

    @NonNull
    @Override
    public String getDisplayName() {
      return Messages.ExtendedTimerTrigger_DisplayName();
    }


    @Override
    public boolean isApplicable(Item item) {
      return item instanceof BuildableItem;
    }

    @POST
    public FormValidation doCheckCronSpec(@QueryParameter String value, @AncestorInPath Item item) {
      try {
        ExtendedCronTabList ectl = ExtendedCronTabList.create(fixNull(value), item != null ? Hash.from(item.getFullName()): null);
        Collection<FormValidation> validations = new ArrayList<>();
        updateValidationsForSanity(validations, ectl);
        updateValidationsForNextRun(validations, ectl);
        return FormValidation.aggregate(validations);
      } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
        return FormValidation.error(e, e.getMessage());
      }
    }

    private void updateValidationsForSanity(Collection<FormValidation> validations, ExtendedCronTabList ectl) {
      String msg = ectl.checkSanity();
      if (msg != null)  validations.add(FormValidation.warning(msg));
    }

    private void updateValidationsForNextRun(Collection<FormValidation> validations, ExtendedCronTabList ectl) {
      ZonedDateTime prev = ectl.previous();
      ZonedDateTime next = ectl.next();
      if (prev != null && next != null) {
        Locale locale = Stapler.getCurrentRequest2() != null
            ? Stapler.getCurrentRequest2().getLocale()
            : Locale.getDefault();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm zzz", locale);
        validations.add(FormValidation.ok(Messages.ExtendedTimerTrigger_would_last_have_run_at_would_next_run_at(prev.format(formatter), next.format(formatter))));
      } else {
        validations.add(FormValidation.warning(Messages.ExtendedTimerTrigger_no_schedules_so_will_never_run()));
      }
    }
  }

  public boolean check(ZonedDateTime time) {
    if (extendedCronTabList != null) {
      return extendedCronTabList.check(time);
    }
    return false;
  }

  public static class ExtendenTimerTriggerCause extends TimerTrigger.TimerTriggerCause {
    @Override
    public String getShortDescription() {
      return Messages.ExtendedTimerTriggerTimerTrigger_ExtendedTimerTriggerCause_ShortDescription();
    }

  }

  @Extension
  public static class CronWork extends PeriodicWork {

    ZonedDateTime zdt = ZonedDateTime.now();

    public CronWork() {
      zdt = zdt.minusSeconds(zdt.getSecond());
    }

    @Override
    public long getRecurrencePeriod() {
      return MIN;
    }

    @Override
    public long getInitialDelay() {
      return MIN - TimeUnit.SECONDS.toMillis(Calendar.getInstance().get(Calendar.SECOND));
    }

    @Override
    public void doRun() {
      ZonedDateTime now = ZonedDateTime.now();
      while (now.isAfter(zdt)) {
        checkTriggers(zdt);
        zdt = zdt.plusMinutes(1);
      }
    }

    private void checkTriggers(final ZonedDateTime time) {
      Jenkins jenkins = Jenkins.get();
      jenkins.allItems(TriggeredItem.class).forEach(item -> {
        for (Trigger<?> t: item.getTriggers().values()) {
          if (t instanceof ExtendedTimerTrigger) {
            ExtendedTimerTrigger trigger = (ExtendedTimerTrigger) t;
            if (trigger.check(time)) {
              trigger.run();
              break;
            }
          }
        }
      });
    }
  }
}
