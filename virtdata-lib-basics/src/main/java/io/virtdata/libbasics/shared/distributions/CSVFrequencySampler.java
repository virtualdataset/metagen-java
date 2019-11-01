package io.virtdata.libbasics.shared.distributions;

/*
 *
 * @author Sebastián Estévez on 10/30/19.
 *
 */


import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.Example;
import io.virtdata.annotations.ThreadSafeMapper;
import io.virtdata.libbasics.core.stathelpers.AliasSamplerDoubleInt;
import io.virtdata.libbasics.core.stathelpers.EvProbD;
import io.virtdata.libbasics.shared.from_long.to_long.Hash;
import io.virtdata.util.VirtDataResources;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.math3.stat.Frequency;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.LongFunction;

/**
 * Takes a CSV with sample data and generates random values based on the
 * relative frequencies of the values in the file.
 * The CSV file must have headers which can
 * be used to find the named columns.
 *
 * I.E. take the following imaginary `animals.csv` file:
 * animal,count,country
 * puppy,1,usa
 * puppy,2,colombia
 * puppy,3,senegal
 * kitten,2,colombia
 *
 * `CSVFrequencySampler('animals.csv', animal)` will return `puppy` or `kitten` randomly. `puppy` will be 3x more frequent than `kitten`.
 *
 * `CSVFrequencySampler('animals.csv', country)` will return `usa`, `columbia`, or `senegal` randomly. `colombia` will be 2x more frequent than `usa` or `senegal`.
 *
 * Use this function to infer frequencies of categorical values from CSVs.
 */

@Categories(Category.general)
@ThreadSafeMapper
public class CSVFrequencySampler implements LongFunction<String> {

    private final String filename;
    private final String columnName;

    private final String[] lines;
    private final AliasSamplerDoubleInt sampler;
    private Hash hash;

    /**
     * Create a sampler of strings from the given CSV file. The CSV file must have plain CSV headers
     * as its first line.
     * @param filename The name of the file to be read into the sampler buffer
     * @param columnName The name of the column to be sampled
     */
    @Example({"CSVFrequencySampler('values.csv','modelno')","Read values.csv, count the frequency of values in 'modelno' column, and sample from this column proportionally"})
    public CSVFrequencySampler(String filename, String columnName) {
        this.filename = filename;
        this.columnName = columnName;

        this.hash=new Hash();

        Set<String> values = new HashSet<>();
        List<EvProbD> frequencies = new ArrayList<>();

        CSVParser csvdata = VirtDataResources.readFileCSV(filename);
        Frequency freq = new Frequency();
        for (CSVRecord csvdatum : csvdata) {
            String value = csvdatum.get(columnName);
            freq.addValue(value);
            values.add(value);
        }
        int i = 0;
        for (String value : values) {
            frequencies.add(new EvProbD(i++,Double.valueOf(freq.getCount(value))));
        }
        sampler = new AliasSamplerDoubleInt(frequencies);
        lines = values.toArray(new String[0]);
    }

    @Override
    public String apply(long value) {
        if (hash!=null) {
            value = hash.applyAsLong(value);
        }
        double unitValue = (double) value / (double) Long.MAX_VALUE;
        int idx = sampler.applyAsInt(unitValue);
        return lines[idx];
    }
}
