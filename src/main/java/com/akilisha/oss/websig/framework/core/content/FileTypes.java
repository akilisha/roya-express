package com.akilisha.oss.web.core.content;

// match - ^(\.(.+?))\s(.+)\s(\b.+\/.+\b)$
// replace - $2("$1", "$3", "$4"),
public enum FileTypes {


    aac(".aac", "AAC audio", "audio/aac"),
    abw(".abw", "AbiWord document", "application/x-abiword"),
    apng(".apng", "Animated Portable Network Graphics (APNG) image", "image/apng"),
    arc(".arc", "Archive document (multiple files embedded)", "application/x-freearc"),
    avif(".avif", "AVIF image", "image/avif"),
    avi(".avi", "AVI: Audio Video Interleave", "video/x-msvideo"),
    azw(".azw", "Amazon Kindle eBook format", "application/vnd.amazon.ebook"),
    bin(".bin", "Any kind of binary data", "application/octet-stream"),
    bmp(".bmp", "Windows OS/2 Bitmap Graphics", "image/bmp"),
    bz(".bz", "BZip archive", "application/x-bzip"),
    bz2(".bz2", "BZip2 archive", "application/x-bzip2"),
    cda(".cda", "CD audio", "application/x-cdf"),
    csh(".csh", "C-Shell script", "application/x-csh"),
    css(".css", "Cascading Style Sheets (CSS)", "text/css"),
    csv(".csv", "Comma-separated values (CSV)", "text/csv"),
    doc(".doc", "Microsoft Word", "application/msword"),
    docx(".docx", "Microsoft Word (OpenXML)", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    eot(".eot", "MS Embedded OpenType fonts", "application/vnd.ms-fontobject"),
    epub(".epub", "Electronic publication (EPUB)", "application/epub+zip"),
    gz(".gz", "GZip Compressed Archive	application/gzip. Note, Windows and macOS upload .gz files with the non-standard MIME type", "application/x-gzip"),
    gif(".gif", "Graphics Interchange Format (GIF)", "image/gif"),
    htm(".htm", "HyperText Markup Language (HTML)", "text/html"),
    html(".html", "HyperText Markup Language (HTML)", "text/html"),
    ico(".ico", "Icon format", "image/vnd.microsoft.icon"),
    ics(".ics", "iCalendar format", "text/calendar"),
    jar(".jar", "Java Archive (JAR)", "application/java-archive"),
    jpeg(".jpeg", "JPEG images", "image/jpeg"),
    jpg(".jpg", "JPEG images", "image/jpeg"),
    js(".js", "JavaScript	text/javascript (Specifications: HTML and RFC 9239)", "application/javascript"),
    json(".json", "JSON format", "application/json"),
    jsonld(".jsonld", "JSON-LD format", "application/ld+json"),
    mid(".mid,", ".midi	Musical Instrument Digital Interface (MIDI)	audio/midi,", "audio/x-midi"),
    mjs(".mjs", "JavaScript module", "text/javascript"),
    mp3(".mp3", "MP3 audio", "audio/mpeg"),
    mp4(".mp4", "MP4 video", "video/mp4"),
    mpeg(".mpeg", "MPEG Video", "video/mpeg"),
    mpkg(".mpkg", "Apple Installer Package", "application/vnd.apple.installer+xml"),
    odp(".odp", "OpenDocument presentation document", "application/vnd.oasis.opendocument.presentation"),
    ods(".ods", "OpenDocument spreadsheet document", "application/vnd.oasis.opendocument.spreadsheet"),
    odt(".odt", "OpenDocument text document", "application/vnd.oasis.opendocument.text"),
    oga(".oga", "Ogg audio", "audio/ogg"),
    ogv(".ogv", "Ogg video", "video/ogg"),
    ogx(".ogx", "Ogg", "application/ogg"),
    opus(".opus", "Opus audio in Ogg container", "audio/ogg"),
    otf(".otf", "OpenType font", "font/otf"),
    png(".png", "Portable Network Graphics", "image/png"),
    pdf(".pdf", "Adobe Portable Document Format (PDF)", "application/pdf"),
    php(".php", "Hypertext Preprocessor (Personal Home Page)", "application/x-httpd-php"),
    ppt(".ppt", "Microsoft PowerPoint", "application/vnd.ms-powerpoint"),
    pptx(".pptx", "Microsoft PowerPoint (OpenXML)", "application/vnd.openxmlformats-officedocument.presentationml.presentation"),
    rar(".rar", "RAR archive", "application/vnd.rar"),
    rtf(".rtf", "Rich Text Format (RTF)", "application/rtf"),
    sh(".sh", "Bourne shell script", "application/x-sh"),
    svg(".svg", "Scalable Vector Graphics (SVG)", "image/svg+xml"),
    tar(".tar", "Tape Archive (TAR)", "application/x-tar"),
    tif(".tif,", ".tiff	Tagged Image File Format (TIFF)", "image/tiff"),
    ts(".ts", "MPEG transport stream", "video/mp2t"),
    ttf(".ttf", "TrueType Font", "font/ttf"),
    txt(".txt", "Text, (generally ASCII or ISO 8859-n)", "text/plain"),
    vsd(".vsd", "Microsoft Visio", "application/vnd.visio"),
    wav(".wav", "Waveform Audio Format", "audio/wav"),
    weba(".weba", "WEBM audio", "audio/webm"),
    webm(".webm", "WEBM video", "video/webm"),
    webp(".webp", "WEBP image", "image/webp"),
    woff(".woff", "Web Open Font Format (WOFF)", "font/woff"),
    woff2(".woff2", "Web Open Font Format (WOFF)", "font/woff2"),
    xhtml(".xhtml", "XHTML", "application/xhtml+xml"),
    xls(".xls", "Microsoft Excel", "application/vnd.ms-excel"),
    xlsx(".xlsx", "Microsoft Excel (OpenXML)", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    xml(".xml", "XML	application/xml is recommended as of RFC 7303 (section 4.1), but text/xml is still used sometimes. You can assign a specific MIME type to a file with .xml extension depending on how its contents are meant to be interpreted. For instance, an Atom feed is application/atom+xml, but application/xml serves as a valid default.", "application/xml"),
    xul(".xul", "XUL", "application/vnd.mozilla.xul+xml"),
    zip(".zip", "ZIP archive	application/zip. Note, Windows uploads .zip files with the non-standard MIME type", "application/x-zip-compressed"),
    _3gp(".3gp", "3GPP audio/video container	video/3gpp; audio/3gpp if it doesn't contain video", "video/3gpp"),
    _3g2(".3g2", "3GPP2 audio/video container	video/3gpp2; audio/3gpp2 if it doesn't contain video", "video/3gpp"),
    _7z(".7z", "7-zip archive", "application/x-7z-compressed");

    final String extension;
    final String kind;
    final String mimeType;

    FileTypes(String extension, String kind, String mimeType) {
        this.extension = extension;
        this.kind = kind;
        this.mimeType = mimeType;
    }
}
