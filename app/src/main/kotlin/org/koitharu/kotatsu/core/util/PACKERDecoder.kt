package org.koitharu.kotatsu.core.util

import org.json.JSONObject

object PACKERDecoder {
	/**
	 * Re-implements Dean Edwards' "P.A.C.K.E.R." decoder.
	 *
	 * This code snippet is converted by LLM from Python code, which is originated to yet another
	 * manhuagui downloader. See those webpages for more information.
	 *
	 * @see manhuagui_downloader:https://github.com/LittleYe233/manhuagui-downloader/manhuagui_downloader/utils.py
	 * @see manhuagui:https://github.com/HSSLC/manhuagui-dlr/blob/v2/manhuagui.py#L274-L283
	 *
	 * @param src The string to be unpacked.
	 * @param syms A list of replacement symbols.
	 *
	 * @return The unpacked JSON object.
	 */
	fun unpack(src: String, syms: List<String>): JSONObject {
		val BASE = 62

		// Convert integer (0–61) to a single base-62 character
		fun base62(n: Int): String = when {
			n < 10 -> n.toString()
			n < 36 -> ('a' + (n - 10)).toString()
			else -> ('A' + (n - 36)).toString()
		}

		// Recursive radix-62 encoding
		fun encode62(num: Int): String =
			if (num >= BASE) encode62(num / BASE) + base62(num % BASE)
			else base62(num)

		// 1× replacement pass
		var working = src
		val c = syms.size
		for (idx in c - 1 downTo 0) {
			val replacement = syms[idx]
			if (replacement.isNotEmpty()) {
				val token = encode62(idx)
				// \b for word-boundary; escape token in case it contains regex metachars
				val pattern = Regex("\\b${Regex.escape(token)}\\b")
				working = pattern.replace(working, replacement)
			}
		}

		// Grab the JSON object literal inside parentheses
		val objRegex = Regex("""\((\{.+\})\)""", RegexOption.DOT_MATCHES_ALL)
		val match = objRegex.find(working)
			?: throw IllegalArgumentException("JSON payload not found after unpacking.")
		val jsonBlob = match.groupValues[1]

		return JSONObject(jsonBlob)
	}
}
