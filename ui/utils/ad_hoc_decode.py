import base64
import json

def is_base64_char(c):
    if c in [ 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l',
              'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w','x', 'y', 'z',
              'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
              'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W','X', 'Y', 'Z',
              '0','1','2','3','4','5','6','7','8','9', '/', '+','='
              ]:

        return True
    else:
        return False
def is_base64_string(s):
    for c in s:
        if not is_base64_char(c):
            return False
    return True

def ad_hoc_base64_decode_content(content):
    buffer=''
    b64s=[]
    #content=content.encode('utf-8')
    for c in content:

        if is_base64_char(c):
            buffer=buffer+c
        else:
            if len(buffer)>11:
                b64s.append(buffer)
            buffer=''

    if len(buffer) > 11:
        b64s.append(buffer)

    new_content=content
    for b64 in b64s:
        padd=(len(b64) % 4)*-1


        if padd==0:
            new_content = new_content.replace(b64, str(base64.standard_b64decode(str(b64))))
        else:
            new_content=new_content.replace(b64, str(base64.standard_b64decode( str(b64)[:padd] )))



    return new_content

if __name__ == "__main__":
    #payload="{\"jobId\":1939666,\"chunkId\":1232,\"type\":\"PROCESSED\",\"items\":[{\"id\":0,\"data\":\"MTQwMTMKeyJzdWJtaXR0ZXIiOjE1MDA3MSwiZm9ybWF0IjoiZWJvZyIsImRhdGFzZXROYW1lIjoiMTUwMDcxLWVib2ciLCJiaWJsaW9ncmFwaGljUmVjb3JkSWQiOiIyMTA4NyIsImNvbXBhcmVSZWNvcmQiOiI8P3htbCB2ZXJzaW9uPScxLjAnIGVuY29kaW5nPSd1dGYtOCc/Pjxjb2xsZWN0aW9uPlxuPHJkZjpSREYgeG1sbnM6Y2M9XCJodHRwOi8vd2ViLnJlc291cmNlLm9yZy9jYy9cIiB4bWxuczpwZ3Rlcm1zPVwiaHR0cDovL3d3dy5ndXRlbmJlcmcub3JnLzIwMDkvcGd0ZXJtcy9cIiB4bWxuczpyZGY9XCJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjXCIgeG1sbnM6ZGNhbT1cImh0dHA6Ly9wdXJsLm9yZy9kYy9kY2FtL1wiIHhtbG5zOmRjdGVybXM9XCJodHRwOi8vcHVybC5vcmcvZGMvdGVybXMvXCIgeG1sbnM6cmRmcz1cImh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDEvcmRmLXNjaGVtYSNcIiB4bWxuczptYXJjcmVsPVwiaHR0cDovL2lkLmxvYy5nb3Yvdm9jYWJ1bGFyeS9yZWxhdG9ycy9cIiB4bWw6YmFzZT1cImh0dHA6Ly93d3cuZ3V0ZW5iZXJnLm9yZy9cIj5cbiAgPHBndGVybXM6ZWJvb2sgcmRmOmFib3V0PVwiZWJvb2tzLzIxMDg3XCI+XG4gICAgPGRjdGVybXM6aGFzRm9ybWF0PlxuICAgICAgPHBndGVybXM6ZmlsZSByZGY6YWJvdXQ9XCJodHRwOi8vd3d3Lmd1dGVuYmVyZy5vcmcvZmlsZXMvMjEwODcvMjEwODctOC56aXBcIj5cbiAgICAgICAgPGRjdGVybXM6bW9kaWZpZWQgcmRmOmRhdGF0eXBlPVwiaHR0cDovL3d3dy53My5vcmcvMjAwMS9YTUxTY2hlbWEjZGF0ZVRpbWVcIj4yMDA3LTA0LTE1VDE0OjAzOjI0PC9kY3Rlcm1zOm1vZGlmaWVkPlxuICAgICAgICA8ZGN0ZXJtczpleHRlbnQgcmRmOmRhdGF0eXBlPVwiaHR0cDovL3d3dy53My5vcmcvMjAwMS9YTUxTY2hlbWEjaW50ZWdlclwiPjE4MTg5NDwvZGN0ZXJtczpleHRlbnQ+XG4gICAgICAgIDxkY3Rlcm1zOmlzRm9ybWF0T2YgcmRmOnJlc291cmNlPVwiZWJvb2tzLzIxMDg3XCIvPlxuICAgICAgICA8ZGN0ZXJtczpmb3JtYXQ+XG4gICAgICAgICAgPHJkZjpEZXNjcmlwdGlvbiByZGY6bm9kZUlEPVwiTjkwZmMxY2QzMWM5MDQzM2VhMTgxNjU3MTQ5NDFlODRjXCI+XG4gICAgICAgICAgICA8cmRmOnZhbHVlIHJkZjpkYXRhdHlwZT1cImh0dHA6Ly9wdXJsLm9yZy9kYy90ZXJtcy9JTVRcIj5hcHBsaWNhdGlvbi96aXA8L3JkZjp2YWx1ZT5cbiAgICAgICAgICAgIDxkY2FtOm1lbWJlck9mIHJkZjpyZXNvdXJjZT1cImh0dHA6Ly9wdXJsLm9yZy9kYy90ZXJtcy9JTVRcIi8+XG4gICAgICAgICAgPC9yZGY6RGVzY3JpcHRpb24+XG4gICAgICAgIDwvZGN0ZXJtczpmb3JtYXQ+XG4gICAgICAgIDxkY3Rlcm1zOmZvcm1hdD5cbiAgICAgICAgICA8cmRmOkRlc2[...]"
    payload="{\"jobId\":1796401,\"chunkId\":252,\"type\":\"PROCESSED\",\"items\":[{\"id\":0,\"data\":\"MjYxCjw/eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04Ij8+CjxlczpyZWZlcmVuY2VkYXRhIHhtbG5zOmVzPSJodHRwOi8vb3NzLmRiYy5kay9ucy9lcyI+PGVzOmluZm8gZm9ybWF0PSJtYXJjMiIgbGFuZ3VhZ2U9ImRhbiIgc3VibWl0dGVyPSI4NzQ4MzAiLz48ZGF0YWlvOnNpbmstdXBkYXRlLXRlbXBsYXRlIHVwZGF0ZVRlbXBsYXRlPSJmZnUiIHhtbG5zOmRhdGFpbz0iZGsuZGJjLmRhdGFpby5wcm9jZXNzaW5nIi8+PC9lczpyZWZlcmVuY2VkYXRhPgozMTUyCjw/eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04Ij8+CjxtYXJjeDpyZWNvcmQgZm9ybWF0PSJkYW5NQVJDMiIgdHlwZT0iQmlibGlvZ3JhcGhpYyIgeG1sbnM6bWFyY3g9ImluZm86bGMveG1sbnMvbWFyY3hjaGFuZ2UtdjEiPjxtYXJjeDpsZWFkZXI+MDAwMDBuICAgIDIyMDAwMDAgICA0NTAwPC9tYXJjeDpsZWFkZXI+PG1hcmN4OmRhdGFmaWVsZCBpbmQxPSIwIiBpbmQyPSIwIiB0YWc9IjAwMSI+PG1hcmN4OnN1YmZpZWxkIGNvZGU9ImEiPjAwMDQwMzc2MzwvbWFyY3g6c3ViZmllbGQ+PG1hcmN4OnN1YmZpZWxkIGNvZGU9ImIiPjg3NDgzMDwvbWFyY3g6c3ViZmllbGQ+PG1hcmN4OnN1YmZpZWxkIGNvZGU9ImMiPjIwMTIwMzI3PC9tYXJjeDpzdWJmaWVsZD48bWFyY3g6c3ViZmllbGQgY29kZT0iZCI+MjAxMTA4MjY8L21hcmN4OnN1YmZpZWxkPjxtYXJjeDpzdWJmaWVsZCBjb2RlPSJmIj5hPC9tYXJjeDpzdWJmaWVsZD48L21hcmN4OmRhdGFmaWVsZD48bWFyY3g6ZGF0YWZpZWxkIGluZDE9IjAiIGluZDI9IjAiIHRhZz0iMDA0Ij48bWFyY3g6c3ViZmllbGQgY29kZT0iciI+bjwvbWFyY3g6c3ViZmllbGQ+PG1hcmN4OnN1YmZpZWxkIGNvZGU9ImEiPmU8L21hcmN4OnN1YmZpZWxkPjwvbWFyY3g6ZGF0YWZpZWxkPjxtYXJjeDpkYXRhZmllbGQgaW5kMT0iMCIgaW5kMj0iMCIgdGFnPSIwMDgiPjxtYXJjeDpzdWJmaWVsZCBjb2RlPSJ0Ij5tPC9tYXJjeDpzdWJmaWVsZD48bWFyY3g6c3ViZmllbGQgY29kZT0idiI+NDwvbWFyY3g6c3ViZmllbGQ+PC9tYXJjeDpkYXRhZmllbGQ+PG1hcmN4OmRhdGFmaWVsZCBpbmQxPSIwIiBpbmQyPSIwIiB0YWc9IjAwOSI+PG1hcmN4OnN1YmZpZWxkIGNvZGU9ImEiPmE8L21hcmN4OnN1YmZpZWxkPjxtYXJjeDpzdWJmaWVsZCBjb2RlPSJnIj54eDwvbWFyY3g6c3ViZmllbGQ+PC9tYXJjeDpkYXRhZmllbGQ+PG1hcmN4OmRhdGFmaWVsZCBpbmQxPSIwIiBpbmQyPSIwIiB0YWc9IjAyMSI+PG1hcmN4OnN1YmZpZWxkIGNvZGU9Im4iPjk3OC0zLTc5MTUtMDQ3OS03PC9tYXJjeDpzdWJmaWVsZD48bWFyY3g6c3ViZmllbGQgY29kZT0iZCI+ZmVzdCBnZWIuIDogRVVSIDE2Ljk1PC9tYXJjeDpzdWJmaWVsZD48L21hcmN4OmRhdGFmaWVsZD48bWFyY3g6ZGF0YWZpZWxkIGluZDE9IjAiIGluZDI9IjAiIHRhZz0iMDg4Ij48ttkuywerGH [...]"
    print(ad_hoc_base64_decode_content(payload))