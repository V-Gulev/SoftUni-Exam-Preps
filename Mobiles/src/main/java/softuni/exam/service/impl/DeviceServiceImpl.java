package softuni.exam.service.impl;

import jakarta.xml.bind.JAXBException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import softuni.exam.dtos.DeviceInputDto;
import softuni.exam.dtos.DevicesImportDto;
import softuni.exam.entities.Device;
import softuni.exam.enums.DeviceType;
import softuni.exam.repository.DeviceRepository;
import softuni.exam.service.DeviceService;
import softuni.exam.service.SaleService;
import softuni.exam.util.ValidationUtil;
import softuni.exam.util.XmlParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class DeviceServiceImpl implements DeviceService {


    private final DeviceRepository deviceRepository;
    private final ModelMapper modelMapper;
    private final ValidationUtil validationUtil;
    private final XmlParser xmlParser;
    private final SaleService saleService;

    public DeviceServiceImpl(DeviceRepository deviceRepository, ModelMapper modelMapper, ValidationUtil validationUtil, XmlParser xmlParser, SaleService saleService) {
        this.deviceRepository = deviceRepository;
        this.modelMapper = modelMapper;
        this.validationUtil = validationUtil;
        this.xmlParser = xmlParser;
        this.saleService = saleService;
    }

    @Override
    public boolean areImported() {
        return deviceRepository.count() > 0;
    }

    @Override
    public String readDevicesFromFile() throws IOException {
        Path path = Path.of("src/main/resources/files/xml/devices.xml");
        return Files.readString(path);
    }

    @Override
    public String importDevices() throws IOException, JAXBException {
        DevicesImportDto importDto = xmlParser.fromXml(readDevicesFromFile(), DevicesImportDto.class);

        StringBuilder sb = new StringBuilder();
        for (DeviceInputDto inputDto : importDto.getInput()) {
            Device device = create(inputDto);

            if (device == null) {
                sb.append(String.format("Invalid device%n"));
            } else {
                sb.append(String.format("Successfully imported device of type %s with brand %s%n", device.getDeviceType(), device.getBrand()));
            }
        }
        return sb.toString();
    }

    @Override
    public String exportDevices() {
        List<Device> devices = deviceRepository.findExportable(DeviceType.SMART_PHONE, 1000.0, 128);
        StringBuilder sb = new StringBuilder();

        for (Device device : devices) {
            sb.append(String.format("Device brand: %s%n", device.getBrand()));
            sb.append(String.format("   *Model: %s%n", device.getModel()));
            sb.append(String.format("   **Storage: %d%n", device.getStorage()));
            sb.append(String.format("   ***Price: %.2f%n", device.getPrice()));
        }

        return sb.toString();
    }

    private Device create(DeviceInputDto deviceInputDto) {
        if (!validationUtil.isValid(deviceInputDto)) {
            return null;
        }

        try {
            Device device = modelMapper.map(deviceInputDto, Device.class);
            if (deviceInputDto.getSale() != null) {
                device.setSale(saleService.getReferenceById(deviceInputDto.getSale()));
            }
            deviceRepository.save(device);
            return device;
        } catch (Exception e) {
            return null;
        }
    }
}
