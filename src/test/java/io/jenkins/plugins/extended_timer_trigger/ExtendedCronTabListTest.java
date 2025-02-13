package io.jenkins.plugins.extended_timer_trigger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.List;
import java.util.Map;
import org.junit.Test;

public class ExtendedCronTabListTest {

  @Test
  public void noParameter() {
    String spec = """
        H 22 * * *
        """;

    ExtendedCronTabList ectl = ExtendedCronTabList.create(spec, null);
    List<CronTabWrapper> wrappers = ectl.getCronTabWrapperList();
    assertThat(wrappers.size(), is(1));
    CronTabWrapper wrapper = wrappers.get(0);
    Map<String, String> parameters = wrapper.getParameters();
    assertThat(parameters, is(nullValue()));
  }

  @Test
  public void singleParameter() {
    String spec = """
        H 22 * * *
          %p1=test
        """;

    ExtendedCronTabList ectl = ExtendedCronTabList.create(spec, null);
    List<CronTabWrapper> wrappers = ectl.getCronTabWrapperList();
    assertThat(wrappers.size(), is(1));
    CronTabWrapper wrapper = wrappers.get(0);
    Map<String, String> parameters = wrapper.getParameters();
    assertThat(parameters, is(notNullValue()));
    assertThat(parameters.get("p1"), is("test"));
  }

  @Test
  public void multipleParameter() {
    String spec = """
        H 22 * * *
          %p1=test
          %p2=test2
          %p3=3
        """;

    ExtendedCronTabList ectl = ExtendedCronTabList.create(spec, null);
    List<CronTabWrapper> wrappers = ectl.getCronTabWrapperList();
    assertThat(wrappers.size(), is(1));
    CronTabWrapper wrapper = wrappers.get(0);
    Map<String, String> parameters = wrapper.getParameters();
    assertThat(parameters, is(notNullValue()));
    assertThat(parameters.size(), is(3));
    assertThat(parameters.get("p1"), is("test"));
    assertThat(parameters.get("p2"), is("test2"));
    assertThat(parameters.get("p3"), is("3"));
  }

  @Test
  public void multipleParamMultiCron() {
    String spec = """
        H 22 * * *
          %p1=test
          %p2=test2
          %p3=3
          %%continuation
        TZ=Europe/London
        H 4 * * 5
        H 10 * * *
          %p1=morning
          %% coffee
          %%
          %%at 8 AM
          %p2=foo
        H 16 * * 5
        """;

    ExtendedCronTabList ectl = ExtendedCronTabList.create(spec, null);
    List<CronTabWrapper> wrappers = ectl.getCronTabWrapperList();
    assertThat(wrappers.size(), is(4));
    CronTabWrapper wrapper = wrappers.get(0);
    Map<String, String> parameters = wrapper.getParameters();
    assertThat(parameters, is(notNullValue()));
    assertThat(parameters.size(), is(3));
    assertThat(parameters.get("p1"), is("test"));
    assertThat(parameters.get("p2"), is("test2"));
    assertThat(parameters.get("p3"), is("3\ncontinuation"));
    wrapper = wrappers.get(2);
    parameters = wrapper.getParameters();
    assertThat(parameters, is(notNullValue()));
    assertThat(parameters.size(), is(2));
    assertThat(parameters.get("p1"), is("morning\n coffee\n\nat 8 AM"));
    assertThat(parameters.get("p2"), is("foo"));
    wrapper = wrappers.get(1);
    parameters = wrapper.getParameters();
    assertThat(parameters, is(nullValue()));
    wrapper = wrappers.get(3);
    parameters = wrapper.getParameters();
    assertThat(parameters, is(nullValue()));
  }

  @Test
  public void singleMultilineParameter() {
    String spec = """
        H 22 * * *
        %p1=test
        %%line2
        """;

    ExtendedCronTabList ectl = ExtendedCronTabList.create(spec, null);
    List<CronTabWrapper> wrappers = ectl.getCronTabWrapperList();
    assertThat(wrappers.size(), is(1));
    CronTabWrapper wrapper = wrappers.get(0);
    Map<String, String> parameters = wrapper.getParameters();
    assertThat(parameters, is(notNullValue()));
    assertThat(parameters.get("p1"), is("test\nline2"));
  }

  @Test
  public void ignoreParamAfterTZ() {
    String spec = """
        H 22 * * *
          %p1=test
          %p2=test2
        TZ=Europe/London
        %p3=3
        %%continuation
        H 4 * * 5
        %%continuation
        """;

    ExtendedCronTabList ectl = ExtendedCronTabList.create(spec, null);
    List<CronTabWrapper> wrappers = ectl.getCronTabWrapperList();
    assertThat(wrappers.size(), is(2));
    CronTabWrapper wrapper = wrappers.get(0);
    Map<String, String> parameters = wrapper.getParameters();
    assertThat(parameters, is(notNullValue()));
    assertThat(parameters.size(), is(2));
    assertThat(parameters.get("p1"), is("test"));
    assertThat(parameters.get("p2"), is("test2"));
    wrapper = wrappers.get(1);
    parameters = wrapper.getParameters();
    assertThat(parameters, is(nullValue()));
  }

}
