package cn.nobeta.dingdong.user.service;

import cn.nobeta.dingdong.user.api.AddressRequest;
import cn.nobeta.dingdong.user.domain.UserAddress;
import cn.nobeta.dingdong.user.mapper.AddressMapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AddressServiceTest {
    @Test
    void clearsExistingDefaultBeforeCreatingNewDefaultAddress() {
        AddressMapper mapper = mock(AddressMapper.class);
        doAnswer(call -> { ((UserAddress) call.getArgument(0)).setId(9L); return 1; }).when(mapper).insert(any());
        UserAddress saved = new UserAddress(); saved.setId(9L); saved.setUserId(1L); saved.setDefaultAddress(true);
        when(mapper.findOwned(9L, 1L)).thenReturn(saved);
        AddressService service = new AddressService(mapper);
        UserAddress result = service.create(1L, new AddressRequest("张三", "13800138000", "陕西省", "西安市", "雁塔区", "科技路 1 号", true));
        assertEquals(9L, result.getId());
        verify(mapper).clearDefault(1L);
        verify(mapper).insert(any(UserAddress.class));
    }
}
