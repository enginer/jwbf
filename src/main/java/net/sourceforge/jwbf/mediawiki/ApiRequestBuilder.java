package net.sourceforge.jwbf.mediawiki;

import net.sourceforge.jwbf.core.RequestBuilder;
import net.sourceforge.jwbf.mediawiki.actions.MediaWiki;

public class ApiRequestBuilder extends RequestBuilder {

  public ApiRequestBuilder() {
    super(MediaWiki.URL_API);
  }

  public ApiRequestBuilder action(String action) {
    param("action", action);
    return this;
  }

  /**
   * @deprecated use json instead (you have to change response handling)
   */
  @Deprecated
  public ApiRequestBuilder formatXml() {
    param("format", "xml");
    return this;
  }

  public ApiRequestBuilder formatJson() {
    param("format", "json");
    return this;
  }
}
