package softuni.exam.service.impl;

import com.google.gson.Gson;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import softuni.exam.dtos.SaleInputDto;
import softuni.exam.entities.Sale;
import softuni.exam.repository.SaleRepository;
import softuni.exam.service.SaleService;
import softuni.exam.service.SellerService;
import softuni.exam.util.ValidationUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class SaleServiceImpl implements SaleService {


    private final SaleRepository saleRepository;
    private final Gson gson;
    private final ValidationUtil validationUtil;
    private final ModelMapper modelMapper;
    private final SellerService sellerService;


    public SaleServiceImpl(SaleRepository saleRepository, Gson gson, ValidationUtil validationUtil, ModelMapper modelMapper, SellerService sellerService) {
        this.saleRepository = saleRepository;
        this.gson = gson;
        this.validationUtil = validationUtil;
        this.modelMapper = modelMapper;
        this.sellerService = sellerService;
    }

    @Override
    public boolean areImported() {
        return saleRepository.count() > 0;
    }

    @Override
    public String readSalesFileContent() throws IOException {
        Path path = Path.of("src/main/resources/files/json/sales.json");
        return Files.readString(path);
    }

    @Override
    public String importSales() throws IOException {
        SaleInputDto[] saleInputDtos = gson.fromJson(readSalesFileContent(), SaleInputDto[].class);

        StringBuilder sb = new StringBuilder();
        for (SaleInputDto saleInputDto : saleInputDtos) {
            Sale sale = create(saleInputDto);

            if (sale == null) {
                sb.append(String.format("Invalid sale%n"));
            } else {
                sb.append(String.format("Successfully imported sale with number %s%n", sale.getNumber()));
            }

        }

        return sb.toString();
    }

    @Override
    public Sale getReferenceById(Long id) {
        return saleRepository.getReferenceById(id);
    }

    private Sale create(SaleInputDto saleInputDto) {
        if (!validationUtil.isValid(saleInputDto)) {
            return null;
        }

        try {
            Sale sale = modelMapper.map(saleInputDto, Sale.class);
            if (saleInputDto.getSeller() != null) {
                sale.setSeller(sellerService.getReferenceById(saleInputDto.getSeller()));
            }

            saleRepository.save(sale);
            return sale;
        } catch (Exception e) {
            return null;
        }

    }
}
