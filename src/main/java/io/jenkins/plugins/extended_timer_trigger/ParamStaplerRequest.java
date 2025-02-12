package io.jenkins.plugins.extended_timer_trigger;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpUpgradeHandler;
import jakarta.servlet.http.Part;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.security.Principal;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.sf.json.JSONObject;
import org.apache.commons.fileupload2.core.FileItem;
import org.kohsuke.stapler.Ancestor;
import org.kohsuke.stapler.BindInterceptor;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.StaplerResponse2;
import org.kohsuke.stapler.WebApp;
import org.kohsuke.stapler.bind.BoundObjectTable;
import org.kohsuke.stapler.lang.Klass;

public class ParamStaplerRequest implements StaplerRequest2 {

  private final String value;

  public ParamStaplerRequest(String value) {
    this.value = value;
  }

  @Override
  public Object getAttribute(String s) {
    return null;
  }

  @Override
  public Enumeration<String> getAttributeNames() {
    return null;
  }

  @Override
  public String getCharacterEncoding() {
    return "";
  }

  @Override
  public void setCharacterEncoding(String s) throws UnsupportedEncodingException {

  }

  @Override
  public int getContentLength() {
    return 0;
  }

  @Override
  public long getContentLengthLong() {
    return 0;
  }

  @Override
  public String getContentType() {
    return "";
  }

  @Override
  public ServletInputStream getInputStream() throws IOException {
    return null;
  }

  @Override
  public String getParameter(String name) {
    return value;
  }

  @Override
  public Enumeration<String> getParameterNames() {
    return null;
  }

  @Override
  public String[] getParameterValues(String name) {
    return new String[] { value };
  }

  @Override
  public Map<String, String[]> getParameterMap() {
    return Map.of();
  }

  @Override
  public String getProtocol() {
    return "";
  }

  @Override
  public String getScheme() {
    return "";
  }

  @Override
  public String getServerName() {
    return "";
  }

  @Override
  public int getServerPort() {
    return 0;
  }

  @Override
  public BufferedReader getReader() throws IOException {
    return null;
  }

  @Override
  public String getRemoteAddr() {
    return "";
  }

  @Override
  public String getRemoteHost() {
    return "";
  }

  @Override
  public void setAttribute(String s, Object o) {

  }

  @Override
  public void removeAttribute(String s) {

  }

  @Override
  public Locale getLocale() {
    return null;
  }

  @Override
  public Enumeration<Locale> getLocales() {
    return null;
  }

  @Override
  public boolean isSecure() {
    return false;
  }

  @Override
  public RequestDispatcher getRequestDispatcher(String s) {
    return null;
  }

  @Override
  public String getRealPath(String s) {
    return "";
  }

  @Override
  public int getRemotePort() {
    return 0;
  }

  @Override
  public String getLocalName() {
    return "";
  }

  @Override
  public String getLocalAddr() {
    return "";
  }

  @Override
  public int getLocalPort() {
    return 0;
  }

  @Override
  public Stapler getStapler() {
    return null;
  }

  @Override
  public WebApp getWebApp() {
    return null;
  }

  @Override
  public String getRestOfPath() {
    return "";
  }

  @Override
  public String getOriginalRestOfPath() {
    return "";
  }

  @Override
  public ServletContext getServletContext() {
    return null;
  }

  @Override
  public AsyncContext startAsync() throws IllegalStateException {
    return null;
  }

  @Override
  public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
    return null;
  }

  @Override
  public boolean isAsyncStarted() {
    return false;
  }

  @Override
  public boolean isAsyncSupported() {
    return false;
  }

  @Override
  public AsyncContext getAsyncContext() {
    return null;
  }

  @Override
  public DispatcherType getDispatcherType() {
    return null;
  }

  @Override
  public String getRequestURIWithQueryString() {
    return "";
  }

  @Override
  public StringBuffer getRequestURLWithQueryString() {
    return null;
  }

  @Override
  public RequestDispatcher getView(Object it, String viewName) throws IOException {
    return null;
  }

  @Override
  public RequestDispatcher getView(Class clazz, String viewName) throws IOException {
    return null;
  }

  @Override
  public RequestDispatcher getView(Klass<?> clazz, String viewName) throws IOException {
    return null;
  }

  @Override
  public String getRootPath() {
    return "";
  }

  @Override
  public String getReferer() {
    return "";
  }

  @Override
  public List<Ancestor> getAncestors() {
    return List.of();
  }

  @Override
  public Ancestor findAncestor(Class type) {
    return null;
  }

  @Override
  public <T> T findAncestorObject(Class<T> type) {
    return null;
  }

  @Override
  public Ancestor findAncestor(Object o) {
    return null;
  }

  @Override
  public boolean hasParameter(String name) {
    return false;
  }

  @Override
  public String getOriginalRequestURI() {
    return "";
  }

  @Override
  public boolean checkIfModified(long timestampOfResource, StaplerResponse2 rsp) {
    return false;
  }

  @Override
  public boolean checkIfModified(Date timestampOfResource, StaplerResponse2 rsp) {
    return false;
  }

  @Override
  public boolean checkIfModified(Calendar timestampOfResource, StaplerResponse2 rsp) {
    return false;
  }

  @Override
  public boolean checkIfModified(long timestampOfResource, StaplerResponse2 rsp, long expiration) {
    return false;
  }

  @Override
  public void bindParameters(Object bean) {

  }

  @Override
  public void bindParameters(Object bean, String prefix) {

  }

  @Override
  public <T> List<T> bindParametersToList(Class<T> type, String prefix) {
    return List.of();
  }

  @Override
  public <T> T bindParameters(Class<T> type, String prefix) {
    return null;
  }

  @Override
  public <T> T bindParameters(Class<T> type, String prefix, int index) {
    return null;
  }

  @Override
  public <T> T bindJSON(Class<T> type, JSONObject src) {
    return null;
  }

  @Override
  public <T> T bindJSON(Type genericType, Class<T> erasure, Object json) {
    return null;
  }

  @Override
  public void bindJSON(Object bean, JSONObject src) {

  }

  @Override
  public <T> List<T> bindJSONToList(Class<T> type, Object src) {
    return List.of();
  }

  @Override
  public BindInterceptor getBindInterceptor() {
    return null;
  }

  @Override
  public BindInterceptor setBindListener(BindInterceptor bindListener) {
    return null;
  }

  @Override
  public BindInterceptor setBindInterceptpr(BindInterceptor bindListener) {
    return null;
  }

  @Override
  public BindInterceptor setBindInterceptor(BindInterceptor bindListener) {
    return null;
  }

  @Override
  public JSONObject getSubmittedForm() throws ServletException {
    return null;
  }

  @Override
  public FileItem getFileItem2(String name) throws ServletException, IOException {
    return null;
  }

  @Override
  public org.apache.commons.fileupload.FileItem getFileItem(String name) throws ServletException, IOException {
    return null;
  }

  @Override
  public boolean isJavaScriptProxyCall() {
    return false;
  }

  @Override
  public BoundObjectTable getBoundObjectTable() {
    return null;
  }

  @Override
  public String createJavaScriptProxy(Object toBeExported) {
    return "";
  }

  @Override
  public RenderOnDemandParameters createJavaScriptProxyParameters(Object toBeExported) {
    return null;
  }

  @Override
  public String getAuthType() {
    return "";
  }

  @Override
  public Cookie[] getCookies() {
    return new Cookie[0];
  }

  @Override
  public long getDateHeader(String s) {
    return 0;
  }

  @Override
  public String getHeader(String s) {
    return "";
  }

  @Override
  public Enumeration<String> getHeaders(String s) {
    return null;
  }

  @Override
  public Enumeration<String> getHeaderNames() {
    return null;
  }

  @Override
  public int getIntHeader(String s) {
    return 0;
  }

  @Override
  public String getMethod() {
    return "";
  }

  @Override
  public String getPathInfo() {
    return "";
  }

  @Override
  public String getPathTranslated() {
    return "";
  }

  @Override
  public String getContextPath() {
    return "";
  }

  @Override
  public String getQueryString() {
    return "";
  }

  @Override
  public String getRemoteUser() {
    return "";
  }

  @Override
  public boolean isUserInRole(String s) {
    return false;
  }

  @Override
  public Principal getUserPrincipal() {
    return null;
  }

  @Override
  public String getRequestedSessionId() {
    return "";
  }

  @Override
  public String getRequestURI() {
    return "";
  }

  @Override
  public StringBuffer getRequestURL() {
    return null;
  }

  @Override
  public String getServletPath() {
    return "";
  }

  @Override
  public HttpSession getSession(boolean b) {
    return null;
  }

  @Override
  public HttpSession getSession() {
    return null;
  }

  @Override
  public String changeSessionId() {
    return "";
  }

  @Override
  public boolean isRequestedSessionIdValid() {
    return false;
  }

  @Override
  public boolean isRequestedSessionIdFromCookie() {
    return false;
  }

  @Override
  public boolean isRequestedSessionIdFromURL() {
    return false;
  }

  @Override
  public boolean isRequestedSessionIdFromUrl() {
    return false;
  }

  @Override
  public boolean authenticate(HttpServletResponse httpServletResponse) throws IOException, ServletException {
    return false;
  }

  @Override
  public void login(String s, String s1) throws ServletException {

  }

  @Override
  public void logout() throws ServletException {

  }

  @Override
  public Collection<Part> getParts() throws IOException, ServletException {
    return List.of();
  }

  @Override
  public Part getPart(String s) throws IOException, ServletException {
    return null;
  }

  @Override
  public <T extends HttpUpgradeHandler> T upgrade(Class<T> aClass) throws IOException, ServletException {
    return null;
  }
}
