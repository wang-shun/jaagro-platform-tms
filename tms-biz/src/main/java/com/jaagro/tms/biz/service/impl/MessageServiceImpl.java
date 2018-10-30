package com.jaagro.tms.biz.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jaagro.constant.UserInfo;
import com.jaagro.tms.api.dto.Message.ListMessageCriteriaDto;
import com.jaagro.tms.api.dto.Message.ListUnReadMsgCriteriaDto;
import com.jaagro.tms.api.dto.Message.MessageReturnDto;
import com.jaagro.tms.api.service.MessageService;
import com.jaagro.tms.biz.entity.Message;
import com.jaagro.tms.biz.mapper.MessageMapperExt;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yj
 * @date 2018/10/29
 */
@Service
@Log4j
public class MessageServiceImpl implements MessageService {
    @Autowired
    private MessageMapperExt messageMapperExt;
    @Autowired
    private CurrentUserService currentUserService;
    /**
     * 分页查询消息
     *
     * @param criteriaDto
     * @return
     */
    @Override
    public PageInfo<MessageReturnDto> listMessageByCriteriaDto(ListMessageCriteriaDto criteriaDto) {
        PageHelper.startPage(criteriaDto.getPageNum(),criteriaDto.getPageSize());
        criteriaDto.setToUserId(currentUserService.getCurrentUser() == null ? null : currentUserService.getCurrentUser().getId());
        List<MessageReturnDto> messageList = messageMapperExt.listMessageByCriteriaDto(criteriaDto);
        return new PageInfo<MessageReturnDto>(messageList);
    }

    /**
     * 将消息置为已读
     *
     * @param messageIdList
     * @return
     */
    @Override
    public boolean readMessages(List<Integer> messageIdList) {
        Integer currentUserId = currentUserService.getCurrentUser() == null ? null : currentUserService.getCurrentUser().getId();
        Integer successNum = messageMapperExt.readMessages(messageIdList,currentUserId);
        if (messageIdList.size() == successNum){
            return true;
        }
        return false;
    }

    /**
     * 获取未读消息列表
     *
     * @param criteriaDto
     * @return
     */
    @Override
    public List<MessageReturnDto> listUnreadMessages(ListUnReadMsgCriteriaDto criteriaDto) {
        if (criteriaDto.getMsgStatus() == null || criteriaDto.getMsgStatus() == 0){
            // 消息状态: 0-未读,1-已读
            criteriaDto.setMsgStatus(0);
        }
        UserInfo currentUser = currentUserService.getCurrentUser();
        Integer currentUserId = currentUser == null ? null : currentUser.getId();
        criteriaDto.setToUserId(currentUserId);
        ListMessageCriteriaDto dto = new ListMessageCriteriaDto();
        BeanUtils.copyProperties(criteriaDto,dto);
        return messageMapperExt.listMessageByCriteriaDto(dto);
    }
}
