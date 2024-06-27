package it.pioppi.database.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import it.pioppi.business.dto.ItemDetailDto;
import it.pioppi.business.dto.ItemDto;
import it.pioppi.business.dto.ProviderDto;
import it.pioppi.business.dto.QuantityTypeDto;
import it.pioppi.database.model.entity.ItemDetailEntity;
import it.pioppi.database.model.entity.ItemEntity;
import it.pioppi.database.model.entity.ProviderEntity;
import it.pioppi.database.model.entity.QuantityTypeEntity;

public class EntityDtoMapper {

    public static ItemEntity dtoToEntity(ItemDto itemDto) {

        ItemEntity itemEntity = new ItemEntity();
        itemEntity.setId(itemDto.getId());
        itemEntity.setName(itemDto.getName());
        itemEntity.setTotPortions(itemDto.getTotPortions());
        itemEntity.setStatus(itemDto.getStatus());
        itemEntity.setBarcode(itemDto.getBarcode());
        itemEntity.setNote(itemDto.getNote());
        itemEntity.setCheckDate(itemDto.getCheckDate());
        itemEntity.setCreationDate(itemDto.getCreationDate());
        itemEntity.setLastUpdate(itemDto.getLastUpdateDate());
        return itemEntity;
    }

    public static List<ItemEntity> dtoToEntity(List<ItemDto> itemDtos) {
        List<ItemEntity> itemEntities = new ArrayList<>();
        for (ItemDto itemDto : itemDtos) {
            itemEntities.add(dtoToEntity(itemDto));
        }
        return itemEntities;
    }

    public static ItemDto entityToDto(ItemEntity itemEntity) {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(itemEntity.getId());
        itemDto.setName(itemEntity.getName());
        itemDto.setTotPortions(itemEntity.getTotPortions());
        itemDto.setStatus(itemEntity.getStatus());
        itemDto.setBarcode(itemEntity.getBarcode());
        itemDto.setNote(itemEntity.getNote());
        itemDto.setCheckDate(itemEntity.getCheckDate());
        itemDto.setCreationDate(itemEntity.getCreationDate());
        itemDto.setLastUpdateDate(itemEntity.getLastUpdate());
        return itemDto;
    }

    public static List<ItemDto> entityToDto(List<ItemEntity> itemEntities) {
        List<ItemDto> itemDtos = new ArrayList<>();
        for (ItemEntity itemEntity : itemEntities) {
            itemDtos.add(entityToDto(itemEntity));
        }
        return itemDtos;
    }

    public static ItemDetailEntity detailDtoToEntity(ItemDetailDto itemDetailDto, UUID itemId) {
        ItemDetailEntity itemDetailEntity = new ItemDetailEntity();
        itemDetailEntity.setId(itemDetailDto.getId());
        itemDetailEntity.setItemId(itemId);
        itemDetailEntity.setQuantityToBeOrdered(itemDetailDto.getQuantityToBeOrdered());
        itemDetailEntity.setOrderedQuantity(itemDetailDto.getOrderedQuantity());
        itemDetailEntity.setPortionsRequiredOnSaturday(itemDetailDto.getPortionsRequiredOnSaturday());
        itemDetailEntity.setPortionsRequiredOnSunday(itemDetailDto.getPortionsRequiredOnSunday());
        itemDetailEntity.setPortionsPerWeekend(itemDetailDto.getPortionsPerWeekend());
        itemDetailEntity.setPortionsOnHoliday(itemDetailDto.getPortionsOnHoliday());
        itemDetailEntity.setMaxPortionsSold(itemDetailDto.getMaxPortionsSold());
        itemDetailEntity.setDeliveryDate(itemDetailDto.getDeliveryDate());
        itemDetailEntity.setCreationDate(itemDetailDto.getCreationDate());
        itemDetailEntity.setLastUpdate(itemDetailDto.getLastUpdateDate());
        return itemDetailEntity;
    }

    public static ItemDetailDto detailEntityToDto(ItemDetailEntity itemDetailEntity, UUID itemId) {
        ItemDetailDto itemDetailDto = new ItemDetailDto();
        itemDetailDto.setId(itemDetailEntity.getId());
        itemDetailDto.setItemId(itemId);
        itemDetailDto.setQuantityToBeOrdered(itemDetailEntity.getQuantityToBeOrdered());
        itemDetailDto.setOrderedQuantity(itemDetailEntity.getOrderedQuantity());
        itemDetailDto.setPortionsRequiredOnSaturday(itemDetailEntity.getPortionsRequiredOnSaturday());
        itemDetailDto.setPortionsRequiredOnSunday(itemDetailEntity.getPortionsRequiredOnSunday());
        itemDetailDto.setPortionsPerWeekend(itemDetailEntity.getPortionsPerWeekend());
        itemDetailDto.setPortionsOnHoliday(itemDetailEntity.getPortionsOnHoliday());
        itemDetailDto.setMaxPortionsSold(itemDetailEntity.getMaxPortionsSold());
        itemDetailDto.setDeliveryDate(itemDetailEntity.getDeliveryDate());
        itemDetailDto.setCreationDate(itemDetailEntity.getCreationDate());
        itemDetailDto.setLastUpdateDate(itemDetailEntity.getLastUpdate());
        return itemDetailDto;
    }

    public static ProviderDto entityToDto(ProviderEntity providerEntity) {
        ProviderDto providerDto = new ProviderDto();
        providerDto.setId(providerEntity.getId());
        providerDto.setName(providerEntity.getName());
        providerDto.setAddress(providerEntity.getAddress());
        providerDto.setEmail(providerEntity.getEmail());
        providerDto.setCreationDate(providerEntity.getCreationDate());
        providerDto.setLastUpdateDate(providerEntity.getLastUpdate());
        providerDto.setItemId(providerEntity.getItemId());
        return providerDto;
    }

    public static ProviderEntity dtoToEntity(ProviderDto providerDto) {
        ProviderEntity providerEntity = new ProviderEntity();
        providerEntity.setId(providerDto.getId());
        providerEntity.setName(providerDto.getName());
        providerEntity.setAddress(providerDto.getAddress());
        providerEntity.setPhoneNumber(providerDto.getPhoneNumber());
        providerEntity.setEmail(providerDto.getEmail());
        providerEntity.setCreationDate(providerDto.getCreationDate());
        providerEntity.setLastUpdate(providerDto.getLastUpdateDate());
        providerEntity.setItemId(providerDto.getItemId());
        return providerEntity;
    }

    public static QuantityTypeDto entityToDto(QuantityTypeEntity quantityTypeEntity) {
        QuantityTypeDto quantityItemDto = new QuantityTypeDto();
        quantityItemDto.setId(quantityTypeEntity.getId());
        quantityItemDto.setItemId(quantityTypeEntity.getItemId());
        quantityItemDto.setQuantityType(quantityTypeEntity.getQuantityType());
        quantityItemDto.setDescription(quantityTypeEntity.getQuantityTypeDescription());
        quantityItemDto.setQuantity(quantityTypeEntity.getQuantityTypeAvailable());
        quantityItemDto.setPurpose(quantityTypeEntity.getPurpose());
        quantityItemDto.setCreationDate(quantityTypeEntity.getCreationDate());
        quantityItemDto.setLastUpdateDate(quantityTypeEntity.getLastUpdate());
        return quantityItemDto;
    }

    public static List<QuantityTypeDto> entitiesToDtos(List<QuantityTypeEntity> quantityTypeEntities) {
        List<QuantityTypeDto> quantityTypeDtos = new ArrayList<>();
        for (QuantityTypeEntity quantityTypeEntity : quantityTypeEntities) {
            quantityTypeDtos.add(entityToDto(quantityTypeEntity));
        }
        return quantityTypeDtos;
    }

    public static QuantityTypeEntity dtoToEntity(QuantityTypeDto quantityItemDto) {
        QuantityTypeEntity quantityTypeEntity = new QuantityTypeEntity();
        quantityTypeEntity.setId(quantityItemDto.getId());
        quantityTypeEntity.setItemId(quantityItemDto.getItemId());
        quantityTypeEntity.setQuantityType(quantityItemDto.getQuantityType());
        quantityTypeEntity.setQuantityTypeDescription(quantityItemDto.getDescription());
        quantityTypeEntity.setQuantityTypeAvailable(quantityItemDto.getQuantity());
        quantityTypeEntity.setPurpose(quantityItemDto.getPurpose());
        quantityTypeEntity.setCreationDate(quantityItemDto.getCreationDate());
        quantityTypeEntity.setLastUpdate(quantityItemDto.getLastUpdateDate());
        return quantityTypeEntity;
    }

    public static List<QuantityTypeEntity> dtosToEntities(List<QuantityTypeDto> quantityTypeDtos) {
        List<QuantityTypeEntity> quantityTypeEntities = new ArrayList<>();
        for (QuantityTypeDto quantityTypeDto : quantityTypeDtos) {
            quantityTypeEntities.add(dtoToEntity(quantityTypeDto));
        }
        return quantityTypeEntities;
    }
}
