package softuni.exam.util;


import jakarta.xml.bind.JAXBException;

public interface XmlParser {

    <T> T fromXml(String xml, Class<T> tClass) throws JAXBException;
}
