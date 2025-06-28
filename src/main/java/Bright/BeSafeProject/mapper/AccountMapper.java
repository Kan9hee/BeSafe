package Bright.BeSafeProject.mapper;

import Bright.BeSafeProject.dto.AccountDTO;
import Bright.BeSafeProject.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AccountMapper {
    AccountMapper INSTANCE = Mappers.getMapper(AccountMapper.class);

    AccountDTO toDto(Account account);
}
