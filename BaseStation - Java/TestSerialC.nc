// $Id: TestSerialC.nc,v 1.7 2010-06-29 22:07:25 scipio Exp $

/*									tab:4
 * Copyright (c) 2000-2005 The Regents of the University  of California.  
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the
 *   distribution.
 * - Neither the name of the University of California nor the names of
 *   its contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright (c) 2002-2003 Intel Corporation
 * All rights reserved.
 *
 * This file is distributed under the terms in the attached INTEL-LICENSE     
 * file. If you do not find these files, copies can be found by writing to
 * Intel Research Berkeley, 2150 Shattuck Avenue, Suite 1300, Berkeley, CA, 
 * 94704.  Attention:  Intel License Inquiry.
 */

/**
 * Application to test that the TinyOS java toolchain can communicate
 * with motes over the serial port. 
 *
 *  @author Gilman Tolle
 *  @author Philip Levis
 *  
 *  @date   Aug 12 2005
 *
 **/
#include "AM.h"
#include "Timer.h"
#include "RadioProtocolMessage.h"
#define BUZZER_ID 100;

module TestSerialC {
  uses {
    interface SplitControl as Control;
    interface Leds;
    interface Boot;
    interface Receive;
    interface AMSend;
    interface Timer<TMilli> as MilliTimer;
    interface Packet as SerialPacket;
    interface Packet as RadioPacket;
    
    interface SplitControl as SerialControl;
    interface SplitControl as RadioControl;
    

    interface Send as UartSend;
    
    interface Receive as RadioReceive[am_id_t id];
    interface Receive as RadioSnoop[am_id_t id];

	interface SplitControl as AMControl;
	interface Packet as RadioPacketSend;
    interface AMPacket as RadioAMPacket;
    interface AMSend as AMSendRadio;



  }
}
implementation {
	enum {
    UART_QUEUE_LEN = 12,
    RADIO_QUEUE_LEN = 12,
  };
	
  message_t packet;

  bool locked = FALSE;
  uint16_t counter = 0;
  uint16_t node_id;
  bool G_base_station;
  bool G_buzzer_off = FALSE;
  bool G_buzzer_enabled = TRUE;
  bool G_motion_enabled = TRUE;
  bool G_propagation_effect_enabled = TRUE;
  // Variaveis para armazenar dados para serem enviados Downlink: BaseStation --> PC
  uint16_t Down_node_id;
  bool Down_base_station;
  bool Down_buzzer_off;
  bool Down_buzzer_enabled;
  bool Down_motion_enabled = 1;
  bool Down_propagate_buzzer;
  bool Down_propagation_effect_enabled;
  bool Down_motion_detected = 1;
  
   // Variaveis para armazenar dados para serem enviados Uplink: BaseStation --> Sensores
  uint16_t Up_node_id;
  bool Up_base_station;
  bool Up_buzzer_off;
  bool Up_buzzer_enabled;
  bool Up_motion_enabled = 1;
  bool Up_propagate_buzzer;
  bool Up_propagation_effect_enabled;
  bool Up_motion_detected = 1;

  message_t uartQueueBufs[UART_QUEUE_LEN];
  message_t * ONE_NOK uartQueue[UART_QUEUE_LEN];
  uint8_t uartIn, uartOut;
  bool uartBusy, uartFull;
  uint8_t data__;
  
    task void uartSendTask();
    
     void dropBlink() {
    call Leds.led2Toggle();
  }

  void failBlink() {
    call Leds.led2Toggle();
  }
  
   // Boot
  event void Boot.booted() {
       uint8_t i;

    for (i = 0; i < UART_QUEUE_LEN; i++)
      uartQueue[i] = &uartQueueBufs[i];
    uartIn = uartOut = 0;
    uartBusy = FALSE;
    uartFull = TRUE;
	call Control.start();
    call RadioControl.start();
    call SerialControl.start();
    call Leds.led2Toggle();
  }
  
  event void RadioControl.startDone(error_t error) {
  }

  event void SerialControl.startDone(error_t error) {
    if (error == SUCCESS) {
      uartFull = FALSE;
    }
  }
  
  event void SerialControl.stopDone(error_t error) {}
  event void RadioControl.stopDone(error_t error) {}

  uint8_t count = 0;
  uint8_t radio_msg_rcv=0;
  
  message_t* ONE receive(message_t* ONE msg, void* payload, uint8_t len);
  
  event message_t *RadioSnoop.receive[am_id_t id](message_t *msg, void *payload, uint8_t len) {
    return receive(msg, payload, len);
  }
  
  event message_t *RadioReceive.receive[am_id_t id](message_t *msg, void *payload, uint8_t len) {
    return receive(msg, payload, len);
  }

  message_t* receive(message_t *msg, void *payload, uint8_t len) {
    message_t *ret = msg;

    atomic {
      if (!uartFull)
	{
	
	  ret = uartQueue[uartIn];
	  uartQueue[uartIn] = msg;

	  uartIn = (uartIn + 1) % UART_QUEUE_LEN;
	
	  if (uartIn == uartOut)
	    uartFull = TRUE;

	  if (!uartBusy)
	    {
	      post uartSendTask();
	      uartBusy = TRUE;
	    }
	}
      else{
			
			dropBlink();
		}
    }
    call Leds.led0Toggle();
    return ret;
  }

  uint8_t tmpLen;
  
  task void uartSendTask() {
		uint8_t len;
		message_t* msg;
		RadioMsg* rcm;
		atomic {
			if (uartIn == uartOut && !uartFull) {
				uartBusy = FALSE;
			return;
		  }
		}

		msg = uartQueue[uartOut];
		tmpLen = len = call RadioPacket.payloadLength(msg);
		//Guardar dados do pacote que chegou via Radio
		rcm = (RadioMsg*)(call RadioPacket.getPayload(msg, sizeof(RadioMsg)));
		Down_node_id = rcm->node_id;
		Down_buzzer_off = rcm->buzzer_off;
		Down_motion_detected =  rcm->motion_detected;
		
		// Se o pacote e de um sensor de movimento
		if((rcm->motion_detected == TRUE) && (G_motion_enabled == TRUE) && (G_buzzer_enabled == TRUE) 
		&& ((rcm->node_id == 1) || (rcm->node_id == 2))){
			
			 if (!locked) {
				 RadioMsg * smsg = 
				 (RadioMsg*)(call RadioPacketSend.getPayload(&packet, sizeof(RadioMsg)));
				 smsg->base_station = TRUE;
				 smsg->motion_detected = TRUE;
				 smsg->node_id = BUZZER_ID;
				 smsg->buzzer_enabled = G_buzzer_enabled;
				 smsg->buzzer_off = FALSE;
				 call Leds.led1On();
				
				 //Enviar mensagem para os buzzers
				  if (call AMSendRadio.send(AM_BROADCAST_ADDR, 
				  &packet, sizeof(RadioMsg)) == SUCCESS) {
					locked = TRUE;
					}
					call Leds.led0Toggle();call Leds.led1Toggle();call Leds.led2Toggle();
				}
			
		}
		 node_id = rcm->node_id;
		//Se receber msg via Radio de um no, activa flag radio_msg_rcv
		if (call UartSend.send(uartQueue[uartOut], len) == SUCCESS) {
			
		  radio_msg_rcv=1; 
		  
		   }else  {
		  failBlink();
		  post uartSendTask();
		}
}
  
      // De x em x tempo verifica se recebeu alguma mensagem via Radio
     event void MilliTimer.fired() {  	
      	//call Leds.led0Off(); call Leds.led2Off();
      	
      	if(radio_msg_rcv){
			counter++;
			if (locked) {
			  return;
			}
			else {
			 
			  RadioMsg * rcm = (RadioMsg*)call SerialPacket.getPayload(&packet, sizeof(RadioMsg));
			  if (rcm == NULL) {return;}
			  if (call SerialPacket.maxPayloadLength() < sizeof(RadioMsg)) {			 
				return;
			  }
				
			  rcm->counter = counter;
			  rcm->node_id = Down_node_id;
			  rcm->base_station = Down_base_station;
			  rcm->buzzer_off = FALSE;
			  rcm->buzzer_enabled = G_buzzer_enabled;
			  rcm->motion_enabled = G_motion_enabled;
			  rcm->propagation_effect_enabled = Down_propagation_effect_enabled;
			  rcm->motion_detected = Down_motion_detected;
			
			  // Envia pacote para PC
			  if (call AMSend.send(AM_BROADCAST_ADDR, &packet, sizeof(RadioMsg)) == SUCCESS) {
				locked = TRUE;
				//call Leds.led2Toggle();
			  }
			}
			radio_msg_rcv=0; 
		}else{return;}
		  //call Sounder.beep(500);
}
 
	


	
  event void UartSend.sendDone(message_t* msg, error_t error) {
    if (error != SUCCESS)
      failBlink();
    else
      atomic
	if (msg == uartQueue[uartOut])
	  {
	    if (++uartOut >= UART_QUEUE_LEN)
	      uartOut = 0;
	    if (uartFull)
	      uartFull = FALSE;
	  }
    post uartSendTask();
  }
  
	//Msg recebida via Serial
  event message_t* Receive.receive(message_t* bufPtr, 
				   void* payload, uint8_t len) 
	{
		if (!locked) {
			if (len != sizeof(RadioMsg)) {
				//call Leds.led1Toggle();
				return bufPtr;
			}
			else {
				//Extrair dados da mensagem que chegou
				 RadioMsg * rmsg = (RadioMsg*)payload;
				 
				 
				//Mensagem a enviar via radio
				RadioMsg * smsg = 
				(RadioMsg*)(call RadioPacketSend.getPayload(&packet, sizeof(RadioMsg)));
				smsg->node_id = rmsg->node_id;
				smsg->base_station = TRUE;
				smsg->buzzer_enabled = G_buzzer_enabled = (rmsg->buzzer_enabled != 0);
				smsg->motion_enabled = G_motion_enabled = (rmsg->motion_enabled != 0);
				smsg->propagation_effect_enabled = G_propagation_effect_enabled = (rmsg->propagation_effect_enabled != 0);
				smsg->buzzer_off = rmsg->buzzer_off;
				smsg->motion_detected = FALSE;
				smsg->tone_detected  = FALSE;
				smsg->enabled  = (rmsg->enabled != 0);
				call Leds.led1Toggle();
			   
				
				if (call AMSendRadio.send(AM_BROADCAST_ADDR, 
				&packet, sizeof(RadioMsg)) == SUCCESS) {
				locked = TRUE;
				}
			}
		  
		 /*  if(rcm->propagate_buzzer & 0x7){
			call Leds.led2Toggle();
		  }
		  
		  
		  /*if (rcm->counter & 0x1) {
		call Leds.led0On();
		  }
		  else {
		call Leds.led0Off();
		  }
		  if (rcm->counter & 0x2) {
		call Leds.led1On();
		  }
		  else {
		call Leds.led1Off();
		  }
		  if (rcm->counter & 0x4) {
		call Leds.led2On();
		  }
		  else {
		call Leds.led2Off();
		  }*/
		 } return bufPtr;
		
  }

  event void AMSend.sendDone(message_t* bufPtr, error_t error) {
    if (&packet == bufPtr) {
      locked = FALSE;
    }
  }

  event void Control.startDone(error_t err) {
    if (err == SUCCESS) {
      call MilliTimer.startPeriodic(100);
    }
  }
  event void Control.stopDone(error_t err) {}
  
  event void AMControl.startDone(error_t err) {
    if (err == SUCCESS) {
    }
    else {
      call AMControl.start();
    }
  }
  event void AMControl.stopDone(error_t err) {
  }
      event void AMSendRadio.sendDone(message_t* msg, error_t err) {
    if (&packet == msg) {
      locked = FALSE;
    }
  }
  
  
}


