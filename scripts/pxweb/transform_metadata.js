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

const rows = contents.split('\n');
const jsonItems = rows.map(row => {
    /*
    CSV-columns by Tilastokeskus:
    Koodi;Indikaattorin nimi;Tuottava tilasto;Tilastolyhenne;SVT aihealue;Järjestys;
    LAST-UPDATE;NEXT-UPDATE;FIRST DATA;LAST DATA;
    Kuvaus;Laskentakaava;Esitettävien desimaalien määrä;Mihin palveluihin kuuluu;Lukutyyppi;Aluejaot
    */
    const cols = row.split(';');
    if (cols.length < 10) {
        return;
    }
    return {
        code: cols[0],
// We get localized name from PXWeb so while updating name from JSON is supported by the code we don't want it in our service
//        name: getLocalized(cols[1]),
        desc: getLocalized(cols[10]),
        source: getLocalized(cols[2]),
        valueType: VALUE_TYPE[cols[14]],
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
