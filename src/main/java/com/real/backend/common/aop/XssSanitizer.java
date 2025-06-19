package com.real.backend.common.aop;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.apache.commons.text.StringEscapeUtils;

public class XssSanitizer {

    private static final PolicyFactory POLICY = new HtmlPolicyBuilder()
        .allowElements("b", "i", "u", "em", "strong", "a", "p", "ul", "ol", "li", "br")
        .allowUrlProtocols("http", "https")
        .allowAttributes("href").onElements("a")
        .toFactory();

    public static String sanitize(String input) {
        String cleaned = POLICY.sanitize(input);
        return StringEscapeUtils.unescapeHtml4(cleaned);
    }
}
