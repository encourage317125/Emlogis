package com.emlogis.script.migration.data.proto

import com.emlogis.script.migration.data.EmployeeUtil
import com.emlogis.script.migration.data.legacy.NotificationType

/**
 * Created by rjackson on 6/7/2015.
 */

long bitValue = 0;

bitValue = EmployeeUtil.setBit(NotificationType.TIME_OFF_REQUEST.getBitMask(), bitValue)

bitValue = EmployeeUtil.setBit(NotificationType.SHIFT_SWAP_REQUEST.getBitMask(), bitValue)

boolean timeOffSet =  EmployeeUtil.isBitSet(NotificationType.TIME_OFF_REQUEST.getBitMask(), bitValue)

boolean shiftSwapSet =  EmployeeUtil.isBitSet(NotificationType.SHIFT_SWAP_REQUEST.getBitMask(), bitValue)

boolean wipSet =  EmployeeUtil.isBitSet(NotificationType.WIP_REQUEST.getBitMask(), bitValue)

boolean changeAvailSet =  EmployeeUtil.isBitSet(NotificationType.CHANGE_AVAILABILITY_REQUEST.getBitMask(), bitValue)

println "timeOffSet is: " + timeOffSet
println "shiftSwapSet is: " + shiftSwapSet
println "wipSet is: " + wipSet
println "changeAvailSet is: " + changeAvailSet
