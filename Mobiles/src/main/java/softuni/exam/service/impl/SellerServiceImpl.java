package softuni.exam.service.impl;

import com.google.gson.Gson;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import softuni.exam.dtos.SellerInputDto;
import softuni.exam.entities.Seller;
import softuni.exam.repository.SellerRepository;
import softuni.exam.service.SellerService;
import softuni.exam.util.ValidationUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class SellerServiceImpl implements SellerService {


    private final SellerRepository sellerRepository;
    private final Gson gson;
    private final ModelMapper modelMapper;
    private final ValidationUtil validationUtil;


    public SellerServiceImpl(SellerRepository sellerRepository, Gson gson, ModelMapper modelMapper, ValidationUtil validationUtil) {
        this.sellerRepository = sellerRepository;
        this.gson = gson;
        this.modelMapper = modelMapper;
        this.validationUtil = validationUtil;
    }

    @Override
    public boolean areImported() {
        return sellerRepository.count() > 0;
    }

    @Override
    public String readSellersFromFile() throws IOException {
        Path path = Path.of("src/main/resources/files/json/sellers.json");
        return Files.readString(path);
    }

    @Override
    public String importSellers() throws IOException {
        SellerInputDto[] sellerInputDtos = gson.fromJson(readSellersFromFile(), SellerInputDto[].class);

        StringBuilder sb = new StringBuilder();
        for (SellerInputDto sellerInputDto : sellerInputDtos) {
            Seller seller = create(sellerInputDto);

            if (seller == null) {
                sb.append(String.format("Invalid seller%n"));
            } else {
                sb.append(String.format("Successfully imported seller %s %s%n", seller.getFirstName(), seller.getLastName()));
            }

        }

        return sb.toString();
    }

    @Override
    public Seller getReferenceById(Long id) {
        return sellerRepository.getReferenceById(id);
    }

    private Seller create(SellerInputDto sellerInputDto) {
        if (!validationUtil.isValid(sellerInputDto)) {
            return null;
        }

        try {
            Seller seller = modelMapper.map(sellerInputDto, Seller.class);
            sellerRepository.save(seller);
            return seller;
        } catch (Exception e) {
            return null;
        }
    }
}
