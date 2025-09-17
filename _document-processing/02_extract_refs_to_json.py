import re
import json

input_file = "output/02_guidelines_result_intermediate.md"
output_md_file = "output/03_guidelines_result_final.md"
output_json_file = "output/guidelines_result_refs.json"

# Regex: match from [ref-x]: until the first empty line
# Here (\d+) captures any integer, and ([\s\S]*?) captures the reference text
pattern = re.compile(
    r'^\[ref-(\d+)\]:\s*([\s\S]*?)(?:\n\s*\n|$)',
    re.MULTILINE
)

refs = {}

with open(input_file, "r", encoding="utf-8") as f:
    content = f.read()

    # Extract references (match.group(1) = the number / match.group(2) = the text)
    for match in pattern.finditer(content):
        ref_number = match.group(1)
        refs[f"[ref-{ref_number}]"] = match.group(2).strip()

# Save references as JSON
with open(output_json_file, "w", encoding="utf-8") as f:
    json.dump(refs, f, ensure_ascii=False, indent=2)

# Remove references from the original content to create a cleaned file
cleaned_content = pattern.sub("", content)

with open(output_md_file, "w", encoding="utf-8") as f:
    f.write(cleaned_content.strip() + "\n")

print(f"Extraction completed. {len(refs)} references found.")
print(f"Cleaned file created: {output_md_file}")
