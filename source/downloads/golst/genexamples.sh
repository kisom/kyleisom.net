#!/bin/sh

TARGET="/home/kyle/code/sites/kyleisom.net/source/downloads/golst"
SOURCE="/home/kyle/code/go/src/github.com/gokyle/golst/listing.go"
LISTING="$(basename ${SOURCE})"

if [ -e "${LISTING}" ]; then
        rm -f "${LISTING}"
fi
cp ${SOURCE} .

FORMATS="html latex md pdf tex"

for FORMAT in ${FORMATS}
do
        golst -o ${FORMAT} ${LISTING}
done

cp * ${TARGET}
