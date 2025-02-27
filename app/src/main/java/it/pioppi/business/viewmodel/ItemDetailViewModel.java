package it.pioppi.business.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import it.pioppi.business.dto.ItemWithDetailAndQuantityTypeDto;

public class ItemDetailViewModel extends ViewModel {

    private final MutableLiveData<ItemWithDetailAndQuantityTypeDto> itemLiveData = new MutableLiveData<>();

    public LiveData<ItemWithDetailAndQuantityTypeDto> getItemLiveData() {
        return itemLiveData;
    }


}

