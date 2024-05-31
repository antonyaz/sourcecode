/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package org.apache.flink.formats.avro.generated;

import org.apache.avro.generic.GenericArray;
import org.apache.avro.specific.SpecificData;
import org.apache.avro.util.Utf8;
import org.apache.avro.message.BinaryMessageEncoder;
import org.apache.avro.message.BinaryMessageDecoder;
import org.apache.avro.message.SchemaStore;

@org.apache.avro.specific.AvroGenerated
public class DifferentSchemaRecord extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  private static final long serialVersionUID = -6143909885521276551L;
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"DifferentSchemaRecord\",\"namespace\":\"org.apache.flink.formats.avro.generated\",\"fields\":[{\"name\":\"otherField1\",\"type\":[\"null\",\"long\"],\"default\":null},{\"name\":\"otherField2\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"otherTime1\",\"type\":\"long\"},{\"name\":\"otherField3\",\"type\":[\"null\",\"double\"],\"default\":null},{\"name\":\"otherField4\",\"type\":[\"null\",\"float\"],\"default\":null},{\"name\":\"otherField5\",\"type\":[\"null\",\"int\"],\"default\":null}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }

  private static SpecificData MODEL$ = new SpecificData();

  private static final BinaryMessageEncoder<DifferentSchemaRecord> ENCODER =
      new BinaryMessageEncoder<DifferentSchemaRecord>(MODEL$, SCHEMA$);

  private static final BinaryMessageDecoder<DifferentSchemaRecord> DECODER =
      new BinaryMessageDecoder<DifferentSchemaRecord>(MODEL$, SCHEMA$);

  /**
   * Return the BinaryMessageEncoder instance used by this class.
   * @return the message encoder used by this class
   */
  public static BinaryMessageEncoder<DifferentSchemaRecord> getEncoder() {
    return ENCODER;
  }

  /**
   * Return the BinaryMessageDecoder instance used by this class.
   * @return the message decoder used by this class
   */
  public static BinaryMessageDecoder<DifferentSchemaRecord> getDecoder() {
    return DECODER;
  }

  /**
   * Create a new BinaryMessageDecoder instance for this class that uses the specified {@link SchemaStore}.
   * @param resolver a {@link SchemaStore} used to find schemas by fingerprint
   * @return a BinaryMessageDecoder instance for this class backed by the given SchemaStore
   */
  public static BinaryMessageDecoder<DifferentSchemaRecord> createDecoder(SchemaStore resolver) {
    return new BinaryMessageDecoder<DifferentSchemaRecord>(MODEL$, SCHEMA$, resolver);
  }

  /**
   * Serializes this DifferentSchemaRecord to a ByteBuffer.
   * @return a buffer holding the serialized data for this instance
   * @throws java.io.IOException if this instance could not be serialized
   */
  public java.nio.ByteBuffer toByteBuffer() throws java.io.IOException {
    return ENCODER.encode(this);
  }

  /**
   * Deserializes a DifferentSchemaRecord from a ByteBuffer.
   * @param b a byte buffer holding serialized data for an instance of this class
   * @return a DifferentSchemaRecord instance decoded from the given buffer
   * @throws java.io.IOException if the given bytes could not be deserialized into an instance of this class
   */
  public static DifferentSchemaRecord fromByteBuffer(
      java.nio.ByteBuffer b) throws java.io.IOException {
    return DECODER.decode(b);
  }

   private java.lang.Long otherField1;
   private java.lang.CharSequence otherField2;
   private long otherTime1;
   private java.lang.Double otherField3;
   private java.lang.Float otherField4;
   private java.lang.Integer otherField5;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>.
   */
  public DifferentSchemaRecord() {}

  /**
   * All-args constructor.
   * @param otherField1 The new value for otherField1
   * @param otherField2 The new value for otherField2
   * @param otherTime1 The new value for otherTime1
   * @param otherField3 The new value for otherField3
   * @param otherField4 The new value for otherField4
   * @param otherField5 The new value for otherField5
   */
  public DifferentSchemaRecord(java.lang.Long otherField1, java.lang.CharSequence otherField2, java.lang.Long otherTime1, java.lang.Double otherField3, java.lang.Float otherField4, java.lang.Integer otherField5) {
    this.otherField1 = otherField1;
    this.otherField2 = otherField2;
    this.otherTime1 = otherTime1;
    this.otherField3 = otherField3;
    this.otherField4 = otherField4;
    this.otherField5 = otherField5;
  }

  public org.apache.avro.specific.SpecificData getSpecificData() { return MODEL$; }
  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call.
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return otherField1;
    case 1: return otherField2;
    case 2: return otherTime1;
    case 3: return otherField3;
    case 4: return otherField4;
    case 5: return otherField5;
    default: throw new IndexOutOfBoundsException("Invalid index: " + field$);
    }
  }

  // Used by DatumReader.  Applications should not call.
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: otherField1 = (java.lang.Long)value$; break;
    case 1: otherField2 = (java.lang.CharSequence)value$; break;
    case 2: otherTime1 = (java.lang.Long)value$; break;
    case 3: otherField3 = (java.lang.Double)value$; break;
    case 4: otherField4 = (java.lang.Float)value$; break;
    case 5: otherField5 = (java.lang.Integer)value$; break;
    default: throw new IndexOutOfBoundsException("Invalid index: " + field$);
    }
  }

  /**
   * Gets the value of the 'otherField1' field.
   * @return The value of the 'otherField1' field.
   */
  public java.lang.Long getOtherField1() {
    return otherField1;
  }


  /**
   * Sets the value of the 'otherField1' field.
   * @param value the value to set.
   */
  public void setOtherField1(java.lang.Long value) {
    this.otherField1 = value;
  }

  /**
   * Gets the value of the 'otherField2' field.
   * @return The value of the 'otherField2' field.
   */
  public java.lang.CharSequence getOtherField2() {
    return otherField2;
  }


  /**
   * Sets the value of the 'otherField2' field.
   * @param value the value to set.
   */
  public void setOtherField2(java.lang.CharSequence value) {
    this.otherField2 = value;
  }

  /**
   * Gets the value of the 'otherTime1' field.
   * @return The value of the 'otherTime1' field.
   */
  public long getOtherTime1() {
    return otherTime1;
  }


  /**
   * Sets the value of the 'otherTime1' field.
   * @param value the value to set.
   */
  public void setOtherTime1(long value) {
    this.otherTime1 = value;
  }

  /**
   * Gets the value of the 'otherField3' field.
   * @return The value of the 'otherField3' field.
   */
  public java.lang.Double getOtherField3() {
    return otherField3;
  }


  /**
   * Sets the value of the 'otherField3' field.
   * @param value the value to set.
   */
  public void setOtherField3(java.lang.Double value) {
    this.otherField3 = value;
  }

  /**
   * Gets the value of the 'otherField4' field.
   * @return The value of the 'otherField4' field.
   */
  public java.lang.Float getOtherField4() {
    return otherField4;
  }


  /**
   * Sets the value of the 'otherField4' field.
   * @param value the value to set.
   */
  public void setOtherField4(java.lang.Float value) {
    this.otherField4 = value;
  }

  /**
   * Gets the value of the 'otherField5' field.
   * @return The value of the 'otherField5' field.
   */
  public java.lang.Integer getOtherField5() {
    return otherField5;
  }


  /**
   * Sets the value of the 'otherField5' field.
   * @param value the value to set.
   */
  public void setOtherField5(java.lang.Integer value) {
    this.otherField5 = value;
  }

  /**
   * Creates a new DifferentSchemaRecord RecordBuilder.
   * @return A new DifferentSchemaRecord RecordBuilder
   */
  public static org.apache.flink.formats.avro.generated.DifferentSchemaRecord.Builder newBuilder() {
    return new org.apache.flink.formats.avro.generated.DifferentSchemaRecord.Builder();
  }

  /**
   * Creates a new DifferentSchemaRecord RecordBuilder by copying an existing Builder.
   * @param other The existing builder to copy.
   * @return A new DifferentSchemaRecord RecordBuilder
   */
  public static org.apache.flink.formats.avro.generated.DifferentSchemaRecord.Builder newBuilder(org.apache.flink.formats.avro.generated.DifferentSchemaRecord.Builder other) {
    if (other == null) {
      return new org.apache.flink.formats.avro.generated.DifferentSchemaRecord.Builder();
    } else {
      return new org.apache.flink.formats.avro.generated.DifferentSchemaRecord.Builder(other);
    }
  }

  /**
   * Creates a new DifferentSchemaRecord RecordBuilder by copying an existing DifferentSchemaRecord instance.
   * @param other The existing instance to copy.
   * @return A new DifferentSchemaRecord RecordBuilder
   */
  public static org.apache.flink.formats.avro.generated.DifferentSchemaRecord.Builder newBuilder(org.apache.flink.formats.avro.generated.DifferentSchemaRecord other) {
    if (other == null) {
      return new org.apache.flink.formats.avro.generated.DifferentSchemaRecord.Builder();
    } else {
      return new org.apache.flink.formats.avro.generated.DifferentSchemaRecord.Builder(other);
    }
  }

  /**
   * RecordBuilder for DifferentSchemaRecord instances.
   */
  @org.apache.avro.specific.AvroGenerated
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<DifferentSchemaRecord>
    implements org.apache.avro.data.RecordBuilder<DifferentSchemaRecord> {

    private java.lang.Long otherField1;
    private java.lang.CharSequence otherField2;
    private long otherTime1;
    private java.lang.Double otherField3;
    private java.lang.Float otherField4;
    private java.lang.Integer otherField5;

    /** Creates a new Builder */
    private Builder() {
      super(SCHEMA$);
    }

    /**
     * Creates a Builder by copying an existing Builder.
     * @param other The existing Builder to copy.
     */
    private Builder(org.apache.flink.formats.avro.generated.DifferentSchemaRecord.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.otherField1)) {
        this.otherField1 = data().deepCopy(fields()[0].schema(), other.otherField1);
        fieldSetFlags()[0] = other.fieldSetFlags()[0];
      }
      if (isValidValue(fields()[1], other.otherField2)) {
        this.otherField2 = data().deepCopy(fields()[1].schema(), other.otherField2);
        fieldSetFlags()[1] = other.fieldSetFlags()[1];
      }
      if (isValidValue(fields()[2], other.otherTime1)) {
        this.otherTime1 = data().deepCopy(fields()[2].schema(), other.otherTime1);
        fieldSetFlags()[2] = other.fieldSetFlags()[2];
      }
      if (isValidValue(fields()[3], other.otherField3)) {
        this.otherField3 = data().deepCopy(fields()[3].schema(), other.otherField3);
        fieldSetFlags()[3] = other.fieldSetFlags()[3];
      }
      if (isValidValue(fields()[4], other.otherField4)) {
        this.otherField4 = data().deepCopy(fields()[4].schema(), other.otherField4);
        fieldSetFlags()[4] = other.fieldSetFlags()[4];
      }
      if (isValidValue(fields()[5], other.otherField5)) {
        this.otherField5 = data().deepCopy(fields()[5].schema(), other.otherField5);
        fieldSetFlags()[5] = other.fieldSetFlags()[5];
      }
    }

    /**
     * Creates a Builder by copying an existing DifferentSchemaRecord instance
     * @param other The existing instance to copy.
     */
    private Builder(org.apache.flink.formats.avro.generated.DifferentSchemaRecord other) {
      super(SCHEMA$);
      if (isValidValue(fields()[0], other.otherField1)) {
        this.otherField1 = data().deepCopy(fields()[0].schema(), other.otherField1);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.otherField2)) {
        this.otherField2 = data().deepCopy(fields()[1].schema(), other.otherField2);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.otherTime1)) {
        this.otherTime1 = data().deepCopy(fields()[2].schema(), other.otherTime1);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.otherField3)) {
        this.otherField3 = data().deepCopy(fields()[3].schema(), other.otherField3);
        fieldSetFlags()[3] = true;
      }
      if (isValidValue(fields()[4], other.otherField4)) {
        this.otherField4 = data().deepCopy(fields()[4].schema(), other.otherField4);
        fieldSetFlags()[4] = true;
      }
      if (isValidValue(fields()[5], other.otherField5)) {
        this.otherField5 = data().deepCopy(fields()[5].schema(), other.otherField5);
        fieldSetFlags()[5] = true;
      }
    }

    /**
      * Gets the value of the 'otherField1' field.
      * @return The value.
      */
    public java.lang.Long getOtherField1() {
      return otherField1;
    }


    /**
      * Sets the value of the 'otherField1' field.
      * @param value The value of 'otherField1'.
      * @return This builder.
      */
    public org.apache.flink.formats.avro.generated.DifferentSchemaRecord.Builder setOtherField1(java.lang.Long value) {
      validate(fields()[0], value);
      this.otherField1 = value;
      fieldSetFlags()[0] = true;
      return this;
    }

    /**
      * Checks whether the 'otherField1' field has been set.
      * @return True if the 'otherField1' field has been set, false otherwise.
      */
    public boolean hasOtherField1() {
      return fieldSetFlags()[0];
    }


    /**
      * Clears the value of the 'otherField1' field.
      * @return This builder.
      */
    public org.apache.flink.formats.avro.generated.DifferentSchemaRecord.Builder clearOtherField1() {
      otherField1 = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /**
      * Gets the value of the 'otherField2' field.
      * @return The value.
      */
    public java.lang.CharSequence getOtherField2() {
      return otherField2;
    }


    /**
      * Sets the value of the 'otherField2' field.
      * @param value The value of 'otherField2'.
      * @return This builder.
      */
    public org.apache.flink.formats.avro.generated.DifferentSchemaRecord.Builder setOtherField2(java.lang.CharSequence value) {
      validate(fields()[1], value);
      this.otherField2 = value;
      fieldSetFlags()[1] = true;
      return this;
    }

    /**
      * Checks whether the 'otherField2' field has been set.
      * @return True if the 'otherField2' field has been set, false otherwise.
      */
    public boolean hasOtherField2() {
      return fieldSetFlags()[1];
    }


    /**
      * Clears the value of the 'otherField2' field.
      * @return This builder.
      */
    public org.apache.flink.formats.avro.generated.DifferentSchemaRecord.Builder clearOtherField2() {
      otherField2 = null;
      fieldSetFlags()[1] = false;
      return this;
    }

    /**
      * Gets the value of the 'otherTime1' field.
      * @return The value.
      */
    public long getOtherTime1() {
      return otherTime1;
    }


    /**
      * Sets the value of the 'otherTime1' field.
      * @param value The value of 'otherTime1'.
      * @return This builder.
      */
    public org.apache.flink.formats.avro.generated.DifferentSchemaRecord.Builder setOtherTime1(long value) {
      validate(fields()[2], value);
      this.otherTime1 = value;
      fieldSetFlags()[2] = true;
      return this;
    }

    /**
      * Checks whether the 'otherTime1' field has been set.
      * @return True if the 'otherTime1' field has been set, false otherwise.
      */
    public boolean hasOtherTime1() {
      return fieldSetFlags()[2];
    }


    /**
      * Clears the value of the 'otherTime1' field.
      * @return This builder.
      */
    public org.apache.flink.formats.avro.generated.DifferentSchemaRecord.Builder clearOtherTime1() {
      fieldSetFlags()[2] = false;
      return this;
    }

    /**
      * Gets the value of the 'otherField3' field.
      * @return The value.
      */
    public java.lang.Double getOtherField3() {
      return otherField3;
    }


    /**
      * Sets the value of the 'otherField3' field.
      * @param value The value of 'otherField3'.
      * @return This builder.
      */
    public org.apache.flink.formats.avro.generated.DifferentSchemaRecord.Builder setOtherField3(java.lang.Double value) {
      validate(fields()[3], value);
      this.otherField3 = value;
      fieldSetFlags()[3] = true;
      return this;
    }

    /**
      * Checks whether the 'otherField3' field has been set.
      * @return True if the 'otherField3' field has been set, false otherwise.
      */
    public boolean hasOtherField3() {
      return fieldSetFlags()[3];
    }


    /**
      * Clears the value of the 'otherField3' field.
      * @return This builder.
      */
    public org.apache.flink.formats.avro.generated.DifferentSchemaRecord.Builder clearOtherField3() {
      otherField3 = null;
      fieldSetFlags()[3] = false;
      return this;
    }

    /**
      * Gets the value of the 'otherField4' field.
      * @return The value.
      */
    public java.lang.Float getOtherField4() {
      return otherField4;
    }


    /**
      * Sets the value of the 'otherField4' field.
      * @param value The value of 'otherField4'.
      * @return This builder.
      */
    public org.apache.flink.formats.avro.generated.DifferentSchemaRecord.Builder setOtherField4(java.lang.Float value) {
      validate(fields()[4], value);
      this.otherField4 = value;
      fieldSetFlags()[4] = true;
      return this;
    }

    /**
      * Checks whether the 'otherField4' field has been set.
      * @return True if the 'otherField4' field has been set, false otherwise.
      */
    public boolean hasOtherField4() {
      return fieldSetFlags()[4];
    }


    /**
      * Clears the value of the 'otherField4' field.
      * @return This builder.
      */
    public org.apache.flink.formats.avro.generated.DifferentSchemaRecord.Builder clearOtherField4() {
      otherField4 = null;
      fieldSetFlags()[4] = false;
      return this;
    }

    /**
      * Gets the value of the 'otherField5' field.
      * @return The value.
      */
    public java.lang.Integer getOtherField5() {
      return otherField5;
    }


    /**
      * Sets the value of the 'otherField5' field.
      * @param value The value of 'otherField5'.
      * @return This builder.
      */
    public org.apache.flink.formats.avro.generated.DifferentSchemaRecord.Builder setOtherField5(java.lang.Integer value) {
      validate(fields()[5], value);
      this.otherField5 = value;
      fieldSetFlags()[5] = true;
      return this;
    }

    /**
      * Checks whether the 'otherField5' field has been set.
      * @return True if the 'otherField5' field has been set, false otherwise.
      */
    public boolean hasOtherField5() {
      return fieldSetFlags()[5];
    }


    /**
      * Clears the value of the 'otherField5' field.
      * @return This builder.
      */
    public org.apache.flink.formats.avro.generated.DifferentSchemaRecord.Builder clearOtherField5() {
      otherField5 = null;
      fieldSetFlags()[5] = false;
      return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public DifferentSchemaRecord build() {
      try {
        DifferentSchemaRecord record = new DifferentSchemaRecord();
        record.otherField1 = fieldSetFlags()[0] ? this.otherField1 : (java.lang.Long) defaultValue(fields()[0]);
        record.otherField2 = fieldSetFlags()[1] ? this.otherField2 : (java.lang.CharSequence) defaultValue(fields()[1]);
        record.otherTime1 = fieldSetFlags()[2] ? this.otherTime1 : (java.lang.Long) defaultValue(fields()[2]);
        record.otherField3 = fieldSetFlags()[3] ? this.otherField3 : (java.lang.Double) defaultValue(fields()[3]);
        record.otherField4 = fieldSetFlags()[4] ? this.otherField4 : (java.lang.Float) defaultValue(fields()[4]);
        record.otherField5 = fieldSetFlags()[5] ? this.otherField5 : (java.lang.Integer) defaultValue(fields()[5]);
        return record;
      } catch (org.apache.avro.AvroMissingFieldException e) {
        throw e;
      } catch (java.lang.Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumWriter<DifferentSchemaRecord>
    WRITER$ = (org.apache.avro.io.DatumWriter<DifferentSchemaRecord>)MODEL$.createDatumWriter(SCHEMA$);

  @Override public void writeExternal(java.io.ObjectOutput out)
    throws java.io.IOException {
    WRITER$.write(this, SpecificData.getEncoder(out));
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumReader<DifferentSchemaRecord>
    READER$ = (org.apache.avro.io.DatumReader<DifferentSchemaRecord>)MODEL$.createDatumReader(SCHEMA$);

  @Override public void readExternal(java.io.ObjectInput in)
    throws java.io.IOException {
    READER$.read(this, SpecificData.getDecoder(in));
  }

  @Override protected boolean hasCustomCoders() { return true; }

  @Override public void customEncode(org.apache.avro.io.Encoder out)
    throws java.io.IOException
  {
    if (this.otherField1 == null) {
      out.writeIndex(0);
      out.writeNull();
    } else {
      out.writeIndex(1);
      out.writeLong(this.otherField1);
    }

    if (this.otherField2 == null) {
      out.writeIndex(0);
      out.writeNull();
    } else {
      out.writeIndex(1);
      out.writeString(this.otherField2);
    }

    out.writeLong(this.otherTime1);

    if (this.otherField3 == null) {
      out.writeIndex(0);
      out.writeNull();
    } else {
      out.writeIndex(1);
      out.writeDouble(this.otherField3);
    }

    if (this.otherField4 == null) {
      out.writeIndex(0);
      out.writeNull();
    } else {
      out.writeIndex(1);
      out.writeFloat(this.otherField4);
    }

    if (this.otherField5 == null) {
      out.writeIndex(0);
      out.writeNull();
    } else {
      out.writeIndex(1);
      out.writeInt(this.otherField5);
    }

  }

  @Override public void customDecode(org.apache.avro.io.ResolvingDecoder in)
    throws java.io.IOException
  {
    org.apache.avro.Schema.Field[] fieldOrder = in.readFieldOrderIfDiff();
    if (fieldOrder == null) {
      if (in.readIndex() != 1) {
        in.readNull();
        this.otherField1 = null;
      } else {
        this.otherField1 = in.readLong();
      }

      if (in.readIndex() != 1) {
        in.readNull();
        this.otherField2 = null;
      } else {
        this.otherField2 = in.readString(this.otherField2 instanceof Utf8 ? (Utf8)this.otherField2 : null);
      }

      this.otherTime1 = in.readLong();

      if (in.readIndex() != 1) {
        in.readNull();
        this.otherField3 = null;
      } else {
        this.otherField3 = in.readDouble();
      }

      if (in.readIndex() != 1) {
        in.readNull();
        this.otherField4 = null;
      } else {
        this.otherField4 = in.readFloat();
      }

      if (in.readIndex() != 1) {
        in.readNull();
        this.otherField5 = null;
      } else {
        this.otherField5 = in.readInt();
      }

    } else {
      for (int i = 0; i < 6; i++) {
        switch (fieldOrder[i].pos()) {
        case 0:
          if (in.readIndex() != 1) {
            in.readNull();
            this.otherField1 = null;
          } else {
            this.otherField1 = in.readLong();
          }
          break;

        case 1:
          if (in.readIndex() != 1) {
            in.readNull();
            this.otherField2 = null;
          } else {
            this.otherField2 = in.readString(this.otherField2 instanceof Utf8 ? (Utf8)this.otherField2 : null);
          }
          break;

        case 2:
          this.otherTime1 = in.readLong();
          break;

        case 3:
          if (in.readIndex() != 1) {
            in.readNull();
            this.otherField3 = null;
          } else {
            this.otherField3 = in.readDouble();
          }
          break;

        case 4:
          if (in.readIndex() != 1) {
            in.readNull();
            this.otherField4 = null;
          } else {
            this.otherField4 = in.readFloat();
          }
          break;

        case 5:
          if (in.readIndex() != 1) {
            in.readNull();
            this.otherField5 = null;
          } else {
            this.otherField5 = in.readInt();
          }
          break;

        default:
          throw new java.io.IOException("Corrupt ResolvingDecoder.");
        }
      }
    }
  }
}









