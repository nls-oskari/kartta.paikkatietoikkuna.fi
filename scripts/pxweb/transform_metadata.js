/**
 * Reads metadata_tk.csv file located in same dir and generates pxweb_metadata_tk.json file based on it.
 * The JSON-file can be configured as "metadataFile" for Oskari PXWeb statistical datasource.
 * The file is used to enhance the metadata we get from the PXWeb API.
 * Indicators are linked with the "code" value to what we parse as indicator id from PXWeb API.
 * 
 * In case of Tilastokeskus where id is something_other.px::[code] and the datasource in db has indicatorKey defined
 * we also try to match the [code] that should match the indicatorKey value to the [code] in the JSON.
 */
const fs = require('fs');
const contents = fs.readFileSync(__dirname + '/metadata_tk.csv', { encoding : 'latin1' }).toString();

// Valuetypes are no longer used in latest CSV :(
const VALUE_TYPE = {
    '%osuus': 'percentage',
    'lkm': 'count',
    '%muu': 'relative change',
    'lkmneg': 'split',
    'suhde': 'ratio'
};

const getLocalized = (value) => {
    if (!value) {
        return null;
    }
    return {
        fi: value
    };
};
const getNum = (value, row) => {
    if (value == 'Koko maa, Kunta, Maakunta, Seutukunta') {
        console.log('Unexpected value for num. Check parsing for row:', row);
        return undefined;
    }
    if ('ääretön' === value) {
        //console.log('Number.POSITIVE_INFINITY')
        return undefined;
    } else if ('negat ääretön' === value) {
        //console.log('Number.NEGATIVE_INFINITY')
        return undefined;
    }
    let num = value;
    try {
        num = Number.parseInt(value);
        if (isNaN(num)) {
            num = undefined;
        }
    } catch(e) {
        num =  undefined;
    }
    if (typeof num === 'undefined') {
        console.log("num: '" + value + "' -> '" + num + "'");
    }
    return num;
}

const isRatio = (lkm) => {
    if (lkm === 'lkm') {
        return false;
    } else if (lkm === 'suhde') {
        return true;
    }
    console.log("Unrecognized ratio value: '" + lkm + "'");
    return undefined;
}

const rows = contents.split('\n');
const jsonItems = rows.map(row => {
    /*
    CSV-columns by Tilastokeskus:
    Koodi;Indikaattorin nimi;Tuottava tilasto;Tilastolyhenne;SVT aihealue;Järjestys;
    LAST-UPDATE;NEXT-UPDATE;FIRST DATA;LAST DATA;
    Kuvaus;Laskentakaava;Esitettävien desimaalien määrä;Mihin palveluihin kuuluu;Lukutyyppi;Aluejaot;min;max;base;lkm
    */
    let cols = row.split(';');
    let skip;
    cols = cols.map((c, index) => {
        if (index == skip) {
            return null;
        }
        if (c.startsWith("\"")) {
            // NOTE! assumes there's only one ; inside quoted value.
            // We should just generate the CSV using tab-separator to workaround ; in cell values...
            console.log('Value had inner ;', cols.join(';'));
            skip = index + 1;
            return c + cols[skip];
        }
        return c;
    }).filter(i => i !== null);
    if (cols.length < 10) {
        return;
    }
    return {
        code: cols[0],
// We get localized name from PXWeb so while updating name from JSON is supported by the code we don't want it in our service
//        name: getLocalized(cols[1]),
        desc: getLocalized(cols[10]),
        source: getLocalized(cols[2]),
        // there's no valueType anymore. Frontend should use min, max, base, isRatio to detect it.
        // valueType: VALUE_TYPE[cols[14]],
        decimalCount: Number(cols[12]),
        timerange: {
            start: cols[8],
            // - commented out since manually updated/end: cols[9]
        },
        // - commented out since manually updated/updated: cols[6],
        // - commented out since manually updated/nextUpdate: cols[7]
        // - commented out since not used/lyhenne: cols[3],
        // - commented out since not used/labels: cols[4] ? cols[4].split(',').map(l => l.trim()) : [],
        // - commented out since not used/prio: Number(cols[5]),
        // - commented out since not used/regionsets: cols[15] ? cols[15].split(',').map(l => l.trim()): []
        // ;min;max;base;lkm
        min: getNum(cols[16], row),
        max: getNum(cols[17], row),
        base: getNum(cols[18], row),
        isRatio: isRatio(cols[19].trim())
    }
})
.filter(item => !!item)
.map(item => {
    const keysWithValues = Object.keys(item).filter(key => item[key] !== null);
    const validValuesItem = {};
    keysWithValues.forEach(key => {
        validValuesItem[key] = item[key];
    });
    return validValuesItem;
});
fs.writeFileSync(__dirname + '/pxweb_metadata_tk.json', JSON.stringify(jsonItems, null, 2));
