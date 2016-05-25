/*
 *	José Dias 	nr 75847
 *	Diogo Ferreira 	nr 76090
 *
 *	Professor: António Grilo
 
 */
#define G_node_id 1
#include "RadioProtocolMessage.h"

module BinarySensorNC
{
  uses interface Leds;
  uses interface Boot;
  uses interface BinarySensor;
  uses interface Packet;
  uses interface AMPacket;
  uses interface AMSend;
  uses interface Receive;
  uses interface SplitControl as AMControl;
  uses interface Mts300Sounder;

}
implementation
{
  //Variables
  uint16_t counter;
  message_t pkt;
  bool busy = FALSE;
  bool flag1=0;
  bool G_enabled = FALSE;
  bool G_buzzer_enabled;
  bool G_motion_enabled = FALSE;

  event void Boot.booted(){
    call BinarySensor.Init();
    call BinarySensor.autoStart();
    call AMControl.start();
    call Leds.led0On();

   }
   
   //------------Mensagem recebida da base station-----------------------------------//
   //---------Verifica se o utilizador pediu para activar/desactivar o sensor--------//
	event message_t* Receive.receive(message_t* msg, void* payload, uint8_t len){
		if (len == sizeof(RadioMsg)) {
			RadioMsg * rmsg;
			 rmsg = (RadioMsg*)payload;
			 
			if(rmsg->base_station  == TRUE) {
				call Leds.led1On();
				G_buzzer_enabled = rmsg->buzzer_enabled;
				G_enabled = rmsg->enabled;
				if((rmsg->node_id == G_node_id )){
					call Leds.led1On();
					G_motion_enabled = rmsg->motion_enabled;
				}
			}
		}
		if(G_enabled==TRUE){
			
		switch(G_motion_enabled){
			case (TRUE): call Leds.led0On();break;
			case (FALSE): call Leds.led0Off();break;
		}
	}else{call Leds.led0Off();}
    return msg;
  }

   //---------Verifica se o sensor foi activado-----------------------------------------//
  event void BinarySensor.statusChanged(bool active){
    
    
    if((active==TRUE) && (busy==FALSE) && (flag1 == 0) && (G_motion_enabled) && G_enabled){
		
        RadioMsg * smsg;
		call Leds.led2On();
        // Extrair os dados da camada inferior
        smsg = (RadioMsg*)(call Packet.getPayload(&pkt, sizeof(RadioMsg)));
        flag1=1;
        if ( smsg == NULL){
			return;
        }
        smsg->node_id = G_node_id;
        //btrpkt->node_id = TOS_NODE_ID;
        smsg->counter = counter; 
		smsg->buzzer_off = FALSE;
		smsg->motion_detected = TRUE;
		smsg->motion_enabled = TRUE;
		smsg->base_station = FALSE;
        if (call AMSend.send(AM_BROADCAST_ADDR, 
          &pkt, sizeof(RadioMsg)) == SUCCESS) {
			busy = TRUE;
		}
		
    }
    if(!active){
		flag1=0;
		//call Leds.led2Off();call Leds.led1Off();
    }
   }

  event void AMControl.startDone(error_t err) {
    if (err == SUCCESS) {
    }
    else {
      call AMControl.start();
    }
  }
  event void AMControl.stopDone(error_t err) {
  }

  event void AMSend.sendDone(message_t* msg, error_t err) {
    if (&pkt == msg) {
      busy = FALSE;
    }
  }

  
  
}
