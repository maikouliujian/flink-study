package flink.examples.sql._05.format.formats.protobuf.row;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import org.apache.flink.types.Row;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import flink.examples.sql._05.format.formats.protobuf.Dog;
import flink.examples.sql._05.format.formats.protobuf.Person;
import flink.examples.sql._05.format.formats.protobuf.Person.Contact;
import flink.examples.sql._05.format.formats.protobuf.Person.ContactType;

public class ProtobufRowDeserializationSchemaTest {

    private Person p;

    private byte[] b;

    private static final String PROTO_DESCRIPTOR_FILE_GENERATOR_CMD =
            "protoc --proto_path ./src/test/proto --descriptor_set_out=./person.desc ./src/test/proto/person.proto";

    private static final String PROTO_JAVA_FILE_GENERATOR_CMD =
            "protoc --proto_path ./src/test/proto --java_out=./ ./src/test/proto/person.proto";

    @Before
    public void initPerson() throws IOException, InterruptedException {
        this.p = Person
                .newBuilder()
                .setName("name")
                .addAllNames(Lists.newArrayList("name1", "name2"))
                .setId(1)
                .addAllIds(Lists.newArrayList(2, 3))
                .setLong(4L)
                .addAllLongs(Lists.newArrayList(5L, 6L))
                .putAllSiMap(new HashMap<String, Integer>() {
                    {
                        put("key1", 7);
                    }
                })
                .putAllSlMap(new HashMap<String, Long>() {
                    {
                        put("key2", 8L);
                    }
                })
                .putAllSdMap(new HashMap<String, Dog>() {
                    {
                        put("key3", Dog.newBuilder().setId(9).setName("dog1").build());
                    }
                })
                .setDog(Dog.newBuilder().setId(10).setName("dog2").build())
                .addAllDogs(Lists.newArrayList(Dog.newBuilder().setId(11).setName("dog3").build()))
                .addAllContacts(Lists.newArrayList(
                        Contact.newBuilder().setNumber("number").setContactType(ContactType.EMAIL).build()))
                .build();

        this.b = this.p.toByteArray();

        String[] cmds = {"bash", "-c", PROTO_DESCRIPTOR_FILE_GENERATOR_CMD};
        Process process = Runtime.getRuntime().exec(cmds, null, new File("./"));

        int exitCode = process.waitFor();


    }

    @Test
    public void deserializationProtobufToRowTest() throws IOException {

        ProtobufRowDeserializationSchema ds = new ProtobufRowDeserializationSchema(Person.class);

        Row row = ds.deserialize(this.b);

        ProtobufRowSerializationSchema s = new ProtobufRowSerializationSchema(Person.class);

        byte[] b = s.serialize(row);

        Assert.assertArrayEquals(this.b, b);

    }

    @Test
    public void deserializationProtobufToRowByDescriptorTest() throws IOException {

        File file = new File("./person.desc");

        FileInputStream fis = new FileInputStream(file);

        byte[] descriptorBytes = new byte[(int) file.length()];

        fis.read(descriptorBytes);

        ProtobufRowDeserializationSchema ds = new ProtobufRowDeserializationSchema(descriptorBytes);

        Row row = ds.deserialize(this.b);

        ProtobufRowSerializationSchema s = new ProtobufRowSerializationSchema(descriptorBytes);

        byte[] b = s.serialize(row);

        Assert.assertArrayEquals(this.b, b);

    }

    @Test
    public void seAndDeseProtobufRowDeserializationSchema() throws IOException, ClassNotFoundException {

        ProtobufRowDeserializationSchema ds = new ProtobufRowDeserializationSchema(Person.class);

        ByteArrayOutputStream bros = new ByteArrayOutputStream();

        ObjectOutputStream oos = new ObjectOutputStream(bros);

        oos.writeObject(ds);

        byte[] b = bros.toByteArray();

        ByteArrayInputStream bris = new ByteArrayInputStream(b);

        ObjectInputStream ois = new ObjectInputStream(bris);

        Object o = ois.readObject();

        Assert.assertTrue(true);

    }

    @Test
    public void seAndDeseProtobufRowDeserializationSchemaByDescriptor() throws IOException, ClassNotFoundException {

        File file = new File("./person.desc");

        FileInputStream fis = new FileInputStream(file);

        byte[] descriptorBytes = new byte[(int) file.length()];

        fis.read(descriptorBytes);

        ProtobufRowDeserializationSchema ds = new ProtobufRowDeserializationSchema(descriptorBytes);

        ByteArrayOutputStream bros = new ByteArrayOutputStream();

        ObjectOutputStream oos = new ObjectOutputStream(bros);

        oos.writeObject(ds);

        byte[] b = bros.toByteArray();

        ByteArrayInputStream bris = new ByteArrayInputStream(b);

        ObjectInputStream ois = new ObjectInputStream(bris);

        Object o = ois.readObject();

        Assert.assertTrue(true);

    }

}
