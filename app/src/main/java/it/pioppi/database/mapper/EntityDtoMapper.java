package it.pioppi.database.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import it.pioppi.business.dto.ItemDetailDto;
import it.pioppi.business.dto.ItemDto;
import it.pioppi.business.dto.ItemTagDto;
import it.pioppi.business.dto.ItemWithProviderDto;
import it.pioppi.business.dto.ProviderDto;
import it.pioppi.business.dto.QuantityTypeDto;
import it.pioppi.database.model.entity.ItemDetailEntity;
import it.pioppi.database.model.entity.ItemEntity;
import it.pioppi.database.model.entity.ItemTagEntity;
import it.pioppi.database.model.entity.ItemWithProviderEntity;
import it.pioppi.database.model.entity.ProviderEntity;
import it.pioppi.database.model.entity.QuantityTypeEntity;

public class EntityDtoMapper {

    public static ItemEntity dtoToEntity(ItemDto itemDto) {
        if(itemDto == null) {
            return null;
        }
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
        if(itemDtos == null) {
            return new ArrayList<>();
        }
        List<ItemEntity> itemEntities = new ArrayList<>();
        for (ItemDto itemDto : itemDtos) {
            itemEntities.add(dtoToEntity(itemDto));
        }
        return itemEntities;
    }

    public static ItemDto entityToDto(ItemEntity itemEntity) {
        if(itemEntity == null) {
            return null;
        }
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
        if(itemEntities == null) {
            return new ArrayList<>();
        }
        List<ItemDto> itemDtos = new ArrayList<>();
        for (ItemEntity itemEntity : itemEntities) {
            itemDtos.add(entityToDto(itemEntity));
        }
        return itemDtos;
    }

    public static ItemDetailEntity detailDtoToEntity(ItemDetailDto itemDetailDto) {
        if(itemDetailDto == null) {
            return null;
        }
        ItemDetailEntity itemDetailEntity = new ItemDetailEntity();
        itemDetailEntity.setId(itemDetailDto.getId());
        itemDetailEntity.setItemId(itemDetailDto.getItemId());
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

    public static ItemDetailDto detailEntityToDto(ItemDetailEntity itemDetailEntity) {
        if(itemDetailEntity == null) {
            return null;
        }
        ItemDetailDto itemDetailDto = new ItemDetailDto();
        itemDetailDto.setId(itemDetailEntity.getId());
        itemDetailDto.setItemId(itemDetailEntity.getItemId());
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
        if(providerEntity == null) {
            return null;
        }
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
        if(providerDto == null) {
            return null;
        }
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
        if(quantityTypeEntity == null) {
            return null;
        }
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
        if (quantityTypeEntities == null) {
            return new ArrayList<>();
        }
        List<QuantityTypeDto> quantityTypeDtos = new ArrayList<>();
        for (QuantityTypeEntity quantityTypeEntity : quantityTypeEntities) {
            quantityTypeDtos.add(entityToDto(quantityTypeEntity));
        }
        return quantityTypeDtos;
    }

    public static QuantityTypeEntity dtoToEntity(QuantityTypeDto quantityItemDto) {
        if(quantityItemDto == null) {
            return null;
        }
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
        if(quantityTypeDtos == null) {
            return new ArrayList<>();
        }
        List<QuantityTypeEntity> quantityTypeEntities = new ArrayList<>();
        for (QuantityTypeDto quantityTypeDto : quantityTypeDtos) {
            quantityTypeEntities.add(dtoToEntity(quantityTypeDto));
        }
        return quantityTypeEntities;
    }


    public static ItemWithProviderEntity dtoToEntity(ItemWithProviderDto itemWithProviderDto) {
        if (itemWithProviderDto == null) {
            return null;
        }
        ItemWithProviderEntity itemWithProviderEntity = new ItemWithProviderEntity();
        itemWithProviderEntity.itemEntity = dtoToEntity(itemWithProviderDto.getItem());
        itemWithProviderEntity.providers = dtosToEntitiesProviders(itemWithProviderDto.getProviders());
        return itemWithProviderEntity;
    }

    public static List<ProviderEntity> dtosToEntitiesProviders(List<ProviderDto> providerDtos) {
        if (providerDtos == null) {
            return new ArrayList<>();
        }
        List<ProviderEntity> providerEntities = new ArrayList<>();
        for (ProviderDto providerDto : providerDtos) {
            providerEntities.add(dtoToEntity(providerDto));
        }
        return providerEntities;
    }

    public static ItemWithProviderDto entityToDto(ItemWithProviderEntity itemWithProviderEntity) {
        if (itemWithProviderEntity == null) {
            return null;
        }
        ItemWithProviderDto itemWithProviderDto = new ItemWithProviderDto();
        itemWithProviderDto.setItem(entityToDto(itemWithProviderEntity.itemEntity));
        itemWithProviderDto.setProviders(entitiesToDtosProviders(itemWithProviderEntity.providers));
        return itemWithProviderDto;
    }

    public static List<ProviderDto> entitiesToDtosProviders(List<ProviderEntity> providerEntities) {
        if (providerEntities == null) {
            return new ArrayList<>();
        }
        List<ProviderDto> providerDtos = new ArrayList<>();
        for (ProviderEntity providerEntity : providerEntities) {
            providerDtos.add(entityToDto(providerEntity));
        }
        return providerDtos;
    }

    public static List<ItemWithProviderDto> entitiesToDtosItemWithProviders(List<ItemWithProviderEntity> itemWithProviderEntities) {
        if (itemWithProviderEntities == null) {
            return new ArrayList<>();
        }
        List<ItemWithProviderDto> itemWithProviderDtos = new ArrayList<>();
        for (ItemWithProviderEntity itemWithProviderEntity : itemWithProviderEntities) {
            itemWithProviderDtos.add(entityToDto(itemWithProviderEntity));
        }
        return itemWithProviderDtos;
    }

    public static List<ItemWithProviderEntity> dtosToEntitiesItemWithProviders(List<ItemWithProviderDto> itemWithProviderDtos) {
        if (itemWithProviderDtos == null) {
            return new ArrayList<>();
        }
        List<ItemWithProviderEntity> itemWithProviderEntities = new ArrayList<>();
        for (ItemWithProviderDto itemWithProviderDto : itemWithProviderDtos) {
            itemWithProviderEntities.add(dtoToEntity(itemWithProviderDto));
        }
        return itemWithProviderEntities;
    }

    public static ItemTagEntity dtoToEntity(ItemTagDto itemTagDto) {
        if(itemTagDto == null) {
            return null;
        }
        ItemTagEntity itemTagEntity = new ItemTagEntity();
        itemTagEntity.setId(itemTagDto.getId());
        itemTagEntity.setName(itemTagDto.getName());
        itemTagEntity.setCreationDate(itemTagDto.getCreationDate());
        itemTagEntity.setLastUpdate(itemTagDto.getLastUpdate());
        return itemTagEntity;
    }

    public static ItemTagDto entityToDto(ItemTagEntity itemTagEntity) {
        if(itemTagEntity == null) {
            return null;
        }
        ItemTagDto itemTagDto = new ItemTagDto();
        itemTagDto.setId(itemTagEntity.getId());
        itemTagDto.setName(itemTagEntity.getName());
        itemTagDto.setCreationDate(itemTagEntity.getCreationDate());
        itemTagDto.setLastUpdate(itemTagEntity.getLastUpdate());
        return itemTagDto;
    }

    public static List<ItemTagEntity> dtosToEntitiesItemTags(List<ItemTagDto> itemTagDtos) {
        if(itemTagDtos == null) {
            return new ArrayList<>();
        }
        List<ItemTagEntity> itemTagEntities = new ArrayList<>();
        for (ItemTagDto itemTagDto : itemTagDtos) {
            itemTagEntities.add(dtoToEntity(itemTagDto));
        }
        return itemTagEntities;
    }

    public static List<ItemTagDto> entitiesToDtosItemTags(List<ItemTagEntity> itemTagEntities) {
        if(itemTagEntities == null) {
            return new ArrayList<>();
        }
        List<ItemTagDto> itemTagDtos = new ArrayList<>();
        for (ItemTagEntity itemTagEntity : itemTagEntities) {
            itemTagDtos.add(entityToDto(itemTagEntity));
        }
        return itemTagDtos;
    }

}
