package io.jenkins.plugins.extended_timer_trigger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.junit.jupiter.api.Test;

class ExtendedCronTabListTest {

  @Test
  void noParameter() {
    String spec = """
        H 22 * * *
        """;

    ExtendedCronTabList ectl = ExtendedCronTabList.create(spec, null);
    List<CronTabWrapper> wrappers = ectl.getCronTabWrapperList();
    assertEquals(1, wrappers.size());
    CronTabWrapper wrapper = wrappers.get(0);
    Map<String, String> parameters = wrapper.getParameters();
    assertNull(parameters);
  }

  @Test
  void singleParameter() {
    String spec = """
        H 22 * * *
          %p1=test
        """;

    ExtendedCronTabList ectl = ExtendedCronTabList.create(spec, null);
    List<CronTabWrapper> wrappers = ectl.getCronTabWrapperList();
    assertEquals(1, wrappers.size());
    CronTabWrapper wrapper = wrappers.get(0);
    Map<String, String> parameters = wrapper.getParameters();
    assertNotNull(parameters);
    assertEquals("test", parameters.get("p1"));
  }

  @Test
  void multipleParameter() {
    String spec = """
        H 22 * * *
          %p1=test
          %p2=test2
          %p3=3
        """;

    ExtendedCronTabList ectl = ExtendedCronTabList.create(spec, null);
    List<CronTabWrapper> wrappers = ectl.getCronTabWrapperList();
    assertEquals(1, wrappers.size());
    CronTabWrapper wrapper = wrappers.get(0);
    Map<String, String> parameters = wrapper.getParameters();
    assertNotNull(parameters);
    assertEquals(3, parameters.size());
    assertEquals("test", parameters.get("p1"));
    assertEquals("test2", parameters.get("p2"));
    assertEquals("3", parameters.get("p3"));
  }

  @Test
  void multipleParamMultiCron() {
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
    assertEquals(4, wrappers.size());
    CronTabWrapper wrapper = wrappers.get(0);
    Map<String, String> parameters = wrapper.getParameters();
    assertNotNull(parameters);
    assertEquals(3, parameters.size());
    assertEquals("test", parameters.get("p1"));
    assertEquals("test2", parameters.get("p2"));
    assertEquals("3\ncontinuation", parameters.get("p3"));
    wrapper = wrappers.get(2);
    parameters = wrapper.getParameters();
    assertNotNull(parameters);
    assertEquals(2, parameters.size());
    assertEquals("morning\n coffee\n\nat 8 AM", parameters.get("p1"));
    assertEquals("foo", parameters.get("p2"));
    wrapper = wrappers.get(1);
    parameters = wrapper.getParameters();
    assertNull(parameters);
    wrapper = wrappers.get(3);
    parameters = wrapper.getParameters();
    assertNull(parameters);
  }

  @Test
  void singleMultilineParameter() {
    String spec = """
        H 22 * * *
        %p1=test
        %%line2
        """;

    ExtendedCronTabList ectl = ExtendedCronTabList.create(spec, null);
    List<CronTabWrapper> wrappers = ectl.getCronTabWrapperList();
    assertEquals(1, wrappers.size());
    CronTabWrapper wrapper = wrappers.get(0);
    Map<String, String> parameters = wrapper.getParameters();
    assertNotNull(parameters);
    assertEquals("test\nline2", parameters.get("p1"));
  }

  @Test
  void ignoreParamAfterTZ() {
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
    assertEquals(2, wrappers.size());
    CronTabWrapper wrapper = wrappers.get(0);
    Map<String, String> parameters = wrapper.getParameters();
    assertNotNull(parameters);
    assertEquals(2, parameters.size());
    assertEquals("test", parameters.get("p1"));
    assertEquals("test2", parameters.get("p2"));
    wrapper = wrappers.get(1);
    parameters = wrapper.getParameters();
    assertNull(parameters);
  }

  @Test
  void testTimeZone() {
    String spec = """
        TZ=Asia/Tokyo
        H 22 * * *
        """;

    ExtendedCronTabList ectl = ExtendedCronTabList.create(spec, null);
    List<CronTabWrapper> wrappers = ectl.getCronTabWrapperList();
    assertEquals(1, wrappers.size());
    CronTabWrapper wrapper = wrappers.get(0);
    assertEquals(wrapper.getCronTab().getTimeZone(), TimeZone.getTimeZone("Asia/Tokyo"));
  }

}
