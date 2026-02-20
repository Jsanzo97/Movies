package jsanzo.movies.common.extensions

fun<T> List<T>.toFormattedString(): String {
    return toString()
        .replace("[", "")
        .replace("]", "")
}