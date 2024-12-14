package com.cdc.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;

public class ParqueFileConverter {
	@SuppressWarnings("deprecation")
	public static byte[] convertStringToParquet(String sampleString) throws IOException {
		// Create an Avro schema for the string
		Schema schema = SchemaBuilder.builder().stringType();

		// Create a Parquet writer
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ParquetWriter<String> writer = AvroParquetWriter.<String>builder(new org.apache.hadoop.fs.Path("/"))
				.withSchema(schema).withCompressionCodec(CompressionCodecName.SNAPPY).build();

		// Write the string to the Parquet file
		writer.write(sampleString);

		// Close the writer
		writer.close();

		// Return the Parquet bytes
		return bos.toByteArray();
	}
}
