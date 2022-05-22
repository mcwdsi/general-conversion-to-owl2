#!sh

echo "[" >> papers-all-2.json

for f in ./papers/*
 do
  cat "$f" >> papers-all-2.json
  echo "," >> papers-all-2.json
done

echo "]" >> papers-all-2.json

