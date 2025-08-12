import re
import json

input_file = "output/guidelines_diabetes_type2_formated_intermediate.md"
output_file = "output/refs.json"
cleaned_file = "output/guidelines_diabetes_type2_result_final.md"

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
with open(output_file, "w", encoding="utf-8") as f:
    json.dump(refs, f, ensure_ascii=False, indent=2)

# Remove references from the original content to create a cleaned file
cleaned_content = pattern.sub("", content)

with open(cleaned_file, "w", encoding="utf-8") as f:
    f.write(cleaned_content.strip() + "\n")

print(f"Extraction completed. {len(refs)} references found.")
print(f"Cleaned file created: {cleaned_file}")
