import re
import json

input_file = "output/03_guidelines_result_final.md"
output_file = "output/guidelines_result_chunks.json"
max_chunk_chars = 2000

chunks = []
current_chunk = {"text": "", "metadata": {"titles": [], "pages": [], "refs": []}}

current_titles = []
current_page = None

ref_pattern = re.compile(r"\[ref-\d+\]")

# ---- Read file and create initial chunks ----
with open(input_file, "r", encoding="utf-8") as f:
    for line in f:
        line = line.rstrip("\n")

        # Detect & add headings
        if line.startswith("#"):
            if current_chunk["text"].strip():
                chunks.append(current_chunk)
            level = len(line) - len(line.lstrip("#"))
            title_text = line.lstrip("#").strip()
            current_titles = current_titles[:level-1] + [title_text]

            current_chunk = {
                "text": "",
                "metadata": {
                    "titles": current_titles.copy(),
                    "pages": [],
                    "refs": []
                }
            }
            continue

        # Detect page
        page_match = re.match(r"<!-- page:\s*(\d+) -->", line)
        if page_match:
            current_page = int(page_match.group(1)) + 1
            continue

        # Detect regular text -> if exist : add the text & the page
        if line.strip():
            if current_page is not None and current_page not in current_chunk["metadata"]["pages"]:
                current_chunk["metadata"]["pages"].append(current_page)

            current_chunk["text"] += line + "\n"

# The last chunk has not yet been appended to the `chunks` list,
# we only append chunks when a new heading is encountered.
if current_chunk["text"].strip() or current_chunk["metadata"]["titles"]:
    chunks.append(current_chunk)

# ---- Split chunks if they exceed max characters ----
final_chunks = []
for chunk in chunks:
    if len(chunk["text"]) <= max_chunk_chars:
        final_chunks.append(chunk)
    else:
        paragraphs = chunk["text"].split("\n\n")
        temp_text = ""
        for para in paragraphs:
            if len(temp_text) + len(para) + 2 > max_chunk_chars:
                final_chunks.append({
                    "text": temp_text.strip(),
                    "metadata": chunk["metadata"].copy()
                })
                temp_text = para + "\n\n"
            else:
                temp_text += para + "\n\n"
        if temp_text.strip():
            final_chunks.append({
                "text": temp_text.strip(),
                "metadata": chunk["metadata"].copy()
            })

# ---- Post-processing: extract all refs from titles and text ----
for chunk in final_chunks:
    all_text = "\n".join(chunk["metadata"]["titles"]) + "\n" + chunk["text"]
    refs_found = ref_pattern.findall(all_text)
    chunk["metadata"]["refs"] = list(dict.fromkeys(refs_found))  # keep unique references

# ---- Save final JSON ----
with open(output_file, "w", encoding="utf-8") as f:
    json.dump(final_chunks, f, ensure_ascii=False, indent=2)

print(f"✅ {len(final_chunks)} final chunks created in {output_file}")