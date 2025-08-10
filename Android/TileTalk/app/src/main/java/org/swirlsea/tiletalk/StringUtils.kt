package org.swirlsea.tiletalk

import java.util.regex.Pattern

fun parseMessageForLink(message: String): Pair<String?, String> {
    val urlPattern = Pattern.compile(
        "(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
                + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
                + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*^@!:/{};']*)",
        Pattern.CASE_INSENSITIVE or Pattern.MULTILINE or Pattern.DOTALL
    )
    val matcher = urlPattern.matcher(message)
    return if (matcher.find()) {
        val url = matcher.group(0)?.trim()
        val text = message.replace(url!!, "").trim()
        Pair(url, text)
    } else {
        Pair(null, message)
    }
}