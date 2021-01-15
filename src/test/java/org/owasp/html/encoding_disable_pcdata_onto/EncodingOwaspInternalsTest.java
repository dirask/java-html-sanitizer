package org.owasp.html.encoding_disable_pcdata_onto;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.owasp.html.EncodingSettings;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

/**

 FIXED by src code recompilation - 2021-01-15

 src code:
 https://github.com/OWASP/java-html-sanitizer
 https://github.com/OWASP/java-html-sanitizer/blob/main/src/main/java/org/owasp/html/Encoding.java

 org.owasp.html.Encoding changes:

 from:
 static void encodePcdataOnto(String plainText, Appendable output)throws IOException {
    encodeHtmlOnto(plainText, output, "{<!-- -->");
 }

 to:
  static void encodePcdataOnto(String plainText, Appendable output)
        // ..
      if (EncodingSettings.isEncodePcdataOnto()) {
          encodeHtmlOnto(plainText, output, "{<!-- -->");
      } else {
          encodeHtmlOnto(plainText, output, "{");
      }
  }

*/
public class EncodingOwaspInternalsTest {

    @Test
    public void shouldEscapeBrackets() {

        EncodingSettings.setEncodePcdataOnto(true);

        PolicyFactory policy = new HtmlPolicyBuilder().toFactory();

        Assertions.assertThat(
                policy.sanitize("{{}}")
        ).isEqualTo("{<!-- -->{}}");
    }

    @Test
    public void shouldNotEscapeBrackets() {

        EncodingSettings.setEncodePcdataOnto(false);

        PolicyFactory policy = new HtmlPolicyBuilder().toFactory();

        Assertions.assertThat(
                policy.sanitize("{{}}")
        ).isEqualTo("{{}}");
    }
}
