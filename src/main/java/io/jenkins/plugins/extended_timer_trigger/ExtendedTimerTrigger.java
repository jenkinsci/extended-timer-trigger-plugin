package io.jenkins.plugins.extended_timer_trigger;

import static hudson.Util.fixNull;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildableItem;
import hudson.model.CauseAction;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.ParametersDefinitionProperty;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import jenkins.triggers.TriggeredItem;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.verb.POST;

/**
 * Trigger that runs a job periodically.
 */
public class ExtendedTimerTrigger extends Trigger<Job<?, ?>> {

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

  public ExtendedCronTabList getExtendedCronTabList() {
    return extendedCronTabList;
  }

  @Override
  public void start(Job<?, ?> project, boolean newInstance) {
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

  private List<ParameterValue> configureParameterValues(Map<String, String> parameterValues) {
    assert job != null : "job must not be null if this was 'started'";
    ParametersDefinitionProperty paramDefProp = job
        .getProperty(ParametersDefinitionProperty.class);
    List<ParameterValue> defValues = new ArrayList<>();

    /* Scan for all parameter with an associated default values */
    for (ParameterDefinition paramDefinition : paramDefProp.getParameterDefinitions()) {
      ParameterValue defaultValue = paramDefinition.getDefaultParameterValue();

      if (parameterValues.containsKey(paramDefinition.getName())) {
        ParamStaplerRequest request = new ParamStaplerRequest(
            parameterValues.get(paramDefinition.getName()));
        ParameterValue value = paramDefinition.createValue(request);
        if (value != null) {
          defValues.add(value);
        } else {
          LOGGER.warning("Cannot create value for " + paramDefinition.getName());
        }
      } else if (defaultValue != null) {
        defValues.add(defaultValue);
      }
    }

    return defValues;
  }

  public void checkAndRun(ZonedDateTime time) {
    if (extendedCronTabList != null) {
      List<CronTabWrapper> cronTabWrapperList = extendedCronTabList.getCronTabWrapperList();
      cronTabWrapperList.stream().filter(it -> it.check(time)).forEach(cronTab -> {
        Map<String, String> parameters = cronTab.getParameters();
        List<Action> actions = new ArrayList<>();
        actions.add(new CauseAction(new ExtendenTimerTriggerCause()));
        if (parameters != null ) {
          actions.add(new ParametersAction(configureParameterValues(parameters)));
        }
        if (job instanceof AbstractProject<?, ?> project) {
          project.scheduleBuild2(0, actions.toArray(new Action[0]));
        } else if (job instanceof WorkflowJob pipeline) {
          pipeline.scheduleBuild2(0,  actions.toArray(new Action[0]));
        }
      });
    }
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
      zdt = zdt.minusSeconds(zdt.getSecond()).plusMinutes(1);
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
          if (t instanceof ExtendedTimerTrigger trigger) {
            trigger.checkAndRun(time);
          }
        }
      });
    }
  }
}
