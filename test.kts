import java.io.File
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.FileInputStream

val factory = XmlPullParserFactory.newInstance()
factory.isNamespaceAware = false
val parser = factory.newPullParser()
parser.setInput(FileInputStream("rss.xml"), null)

val titles = mutableListOf<String>()
var eventType = parser.eventType
var isInsideItem = false
var currentText = ""

while (eventType != XmlPullParser.END_DOCUMENT && titles.size < 5) {
    val tagName = parser.name
    when (eventType) {
        XmlPullParser.START_TAG -> {
            if (tagName.equals("item", ignoreCase = true)) {
                isInsideItem = true
            }
        }
        XmlPullParser.TEXT, 5 -> { // 5 is CDSECT
            currentText = parser.text ?: ""
        }
        XmlPullParser.END_TAG -> {
            if (tagName.equals("item", ignoreCase = true)) {
                isInsideItem = false
            } else if (tagName.equals("title", ignoreCase = true) && isInsideItem) {
                titles.add(currentText.trim())
            }
        }
    }
    eventType = parser.next()
}
println("Found titles: $titles")
