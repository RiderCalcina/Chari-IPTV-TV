package com.example.chariiptv.domain

import com.example.chariiptv.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import javax.inject.Inject

interface RssRepository {
    suspend fun fetchRssFeed(): Result<List<String>>
}

class RssRepositoryImpl @Inject constructor(
    private val okHttpClient: OkHttpClient
) : RssRepository {

    override suspend fun fetchRssFeed(): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(Constants.RSS_URL)
                .build()

            val textContent = okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("Error fetching RSS: ${response.code}"))
                }
                response.body?.string()?.trimStart() ?: ""
            }
            if (textContent.isEmpty()) {
                return@withContext Result.failure(Exception("Empty RSS content"))
            }

            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = false
            val parser = factory.newPullParser()
            parser.setInput(java.io.StringReader(textContent))

            val titles = mutableListOf<String>()
            var eventType = parser.eventType
            var isInsideItem = false

            while (eventType != XmlPullParser.END_DOCUMENT && titles.size < 5) {
                val tagName = parser.name
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        if (tagName.equals("item", ignoreCase = true)) {
                            isInsideItem = true
                        } else if (tagName.equals("title", ignoreCase = true) && isInsideItem) {
                            val titleText = parser.nextText()
                            titles.add(titleText.trim())
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (tagName.equals("item", ignoreCase = true)) {
                            isInsideItem = false
                        }
                    }
                }
                eventType = parser.next()
            }

            Result.success(titles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
