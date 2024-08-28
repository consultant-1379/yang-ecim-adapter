/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */

package com.ericsson.ranexplorer.yangecimadapter.metrics.handler.record;

import com.ericsson.ranexplorer.yangecimadapter.metrics.handler.MessageState;
import com.ericsson.ranexplorer.yangecimadapter.metrics.handler.MessageStateInfo;
import com.ericsson.ranexplorer.yangecimadapter.metrics.handler.RpcOperation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MessageRecord extends Record {

    private static final String RECORD_NAME = "RPC_MESSAGE";
    private static final String EXTENDED_RECORD_NAME = "EXTENDED_RPC_MESSAGE";
    private final int sessionId;
    private String messageId;
    private RpcOperation rpcOperation = RpcOperation.UNKNOWN;

    private Map<MessageState, MessageStateInfo> states = new ConcurrentHashMap<>();
    private Map<MessageState, MessageStateInfo> extendedMessageRecords = new ConcurrentHashMap<>();

    public MessageRecord(final int sessionId) {
        this.sessionId = sessionId;
    }

    public void setMessageId(final String messageId) {
        this.messageId = messageId;
    }

    public void setRpcOperation(final RpcOperation rpcOperation) {
        this.rpcOperation = rpcOperation;
    }

    public void setStartTime(final MessageState messageState, final long startTime) {
        switch (messageState) {
            case MESSAGE_REQUEST:
                states.put(messageState, new MessageStateInfo(startTime));
                break;
            case MESSAGE_RESPONSE:
                states.put(messageState, new MessageStateInfo(startTime));
                setEndTimeIfNotSet(MessageState.MESSAGE_REQUEST, startTime);
                break;
            default:
                extendedMessageRecords.put(messageState, new MessageStateInfo(startTime));
                if (isErrorOrExceptionState(messageState)) {
                    setEndTimeIfNotSet(MessageState.MESSAGE_REQUEST, startTime);
                    setStartTimeIfNotSet(MessageState.MESSAGE_RESPONSE, startTime);
                    setExtendedRecordStatesEndTimeIfNotSet(startTime);
                }
                break;
        }
    }

    public void setEndTime(final MessageState messageState, final long endTime) {
        MessageStateInfo stateInfo;
        switch (messageState) {
            case MESSAGE_REQUEST:
                stateInfo = states.get(messageState);
                stateInfo.setEndTime(endTime);
                break;
            case MESSAGE_RESPONSE:
                setEndTimeIfNotSet(MessageState.MESSAGE_REQUEST, endTime);
                setStartTimeIfNotSet(MessageState.MESSAGE_RESPONSE, endTime);
                stateInfo = states.get(messageState);
                stateInfo.setEndTime(endTime);
                if (hasMessageStateAndEndTimeNotSet(MessageState.EXCEPTION)) {
                    setEndTime(MessageState.EXCEPTION, endTime);
                } else if (hasMessageStateAndEndTimeNotSet(MessageState.ERROR)) {
                    setEndTime(MessageState.ERROR, endTime);
                }
                break;
            default:
                stateInfo = extendedMessageRecords.get(messageState);
                stateInfo.setEndTime(endTime);
                break;
        }
    }

    public void setStartTimeIfNotSet(final MessageState messageState, final long startTime) {
        MessageStateInfo stateInfo = states.get(messageState);
        if (stateInfo == null) {
            states.put(messageState, new MessageStateInfo(startTime));
        }
    }

    public void setEndTimeIfNotSet(final MessageState messageState, final long endTime) {
        MessageStateInfo stateInfo = states.get(messageState);
        if (stateInfo.isEndTimeNotSet()) {
            stateInfo.setEndTime(endTime);
        }
    }

    public void setExtendedRecordStatesEndTimeIfNotSet(final long endTime) {
        for (MessageState state : MessageState.values()) {
            MessageStateInfo stateInfo = extendedMessageRecords.get(state);
            if (stateInfo != null && stateInfo.isEndTimeNotSet()) {
                stateInfo.setEndTime(endTime);
            }
        }
    }

    private boolean isErrorOrExceptionState(final MessageState messageState){
        return (messageState == MessageState.ERROR) || (messageState == MessageState.EXCEPTION);
    }

    private boolean hasMessageStateAndEndTimeNotSet(final MessageState messageState){
        MessageStateInfo stateInfo = extendedMessageRecords.get(messageState);
        if (stateInfo == null){
            return false;
        }
        return stateInfo.isEndTimeNotSet();
    }

    private int getTimeSpentInNetworkElement(MessageStateInfo request, MessageStateInfo response) {
        return (int) (response.getStartTime() - request.getEndTime());
    }

    public void log(final String southboundSessionId) {
        MessageStateInfo request = states.get(MessageState.MESSAGE_REQUEST);
        MessageStateInfo response = states.get(MessageState.MESSAGE_RESPONSE);
        logInfo(RECORD_NAME, print(sessionId), southboundSessionId,
                messageId,
                rpcOperation.getTagName(),
                print(request.getStartTime()/1000.0),
                print(request.getEndTime()/1000.0),
                print(request.duration()),
                print(getTimeSpentInNetworkElement(request, response)),
                print(response.getStartTime()/1000.0),
                print(response.getEndTime()/1000.0),
                print(response.duration()));
    }

    public void logExtendedMessageRecord(final String southboundSessionId) {
        logDebug(EXTENDED_RECORD_NAME ,print(sessionId), southboundSessionId, messageId,
                rpcOperation.getTagName(),
                generateMessageStateInfo());
    }

    private String generateMessageStateInfo(){
        StringBuilder stringBuilder = new StringBuilder();
        for(MessageState state: MessageState.values()){
            if(extendedMessageRecords.containsKey(state)){
                stringBuilder.append(extendedMessageRecords.get(state).duration());
            }
            else if(states.containsKey(state)){
                stringBuilder.append(states.get(state).duration());
            }
            else {
                stringBuilder.append("0");
            }
            stringBuilder.append(SEPERATOR);
        }
        stringBuilder.deleteCharAt(stringBuilder.length()-1);
        return stringBuilder.toString();
    }
}
