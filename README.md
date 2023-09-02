# Java CSV-Reader and CSV-Writer

CsvReader can read .csv files in any characterset encoding (also with BOM) and with configurable separator and optional stringquote (delimiter).

CsvWriter can write .csv files and supports the same features as CsvReader.

There are also some other convenient optional configuration values.

General features:

    - Using data streams instead of files only
    - Considers linebreaks within quoted data
    - Considers string quote character within quoted data
    - Configurable separator character
    - Configurable optional string quote character
    - Configurable allows too short csvdata lines (trailing empty columns)
    - Read line per line or all data at once

Format features:
- Separating character can be configured
- Character for string quotes can be configured
- Character to escape the string quote character within quoted strings can be configured
	By default this is the stringquote character itself, so it is doubled in quoted strings, but may also be configured to a backslash '\'
- Allow linebreaks in data texts without the effect of a new data set line or use "\\n" to escape linebreaks on csv output
- Allow escaped stringquotes to use them as a character in data text. May be turned off for data consistency checks. */
- Allow lines with less than the expected number of data entries per line can be configured
- Allow lines with more than the expected number of data entries per line, if those are empty. */
- Trim all data values
- Quote data entries: NO_QUOTE, QUOTE_IF_NEEDED, QUOTE_STRINGS, QUOTE_ALL_DATA
- Linebreak character for output can be configured
- Ignore empty lines can be configured
- Use headers in first csv line can be configured
