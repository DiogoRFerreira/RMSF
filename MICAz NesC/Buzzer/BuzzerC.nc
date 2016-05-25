
/*
 *	José Dias 	nr 75847
 *	Diogo Ferreira 	nr 76090
 *
 *	Professor: António Grilo
 */
 
 
#include "Timer.h"
#include "RadioProtocolMessage.h"
#define BUZZER_ID 100
#define ATM128_I2C_EXTERNAL_PULLDOWN 1
// The MTS300 does not have an external pullup resistor for I2C
#if !defined(ATM128_I2C_EXTERNAL_PULLDOWN) || !(ATM128_I2C_EXTERNAL_PULLDOWN)
#error You must set ATM128_I2C_EXTERNAL_PULLDOWN=1
#endif

module ToneDetectorC
{
  uses interface Leds;
  uses interface Boot;
  
	
  uses interface Packet;
  uses interface AMPacket;
  uses interface AMSend;
  uses interface Receive;
  uses interface SplitControl as AMControl;

  uses interface Mts300Sounder as Sounder;
}
implementation
{
  //Variables
  uint16_t counter;
  uint8_t tone_flag;
  message_t pkt;
  bool busy = FALSE;
  bool flag1=0;
  bool ringing = FALSE;
  bool motion_enabled = TRUE;	

  bool G_buzzer_enabled = TRUE;
  bool G_enabled = FALSE;
  
  bool G_propagate_buzzer = TRUE;	
	
  event void Boot.booted()
  { 	
	call AMControl.start();
	call Leds.led0On();

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

   
  
   //------------Mensagem recebida da base station-----------------------------------//
   //---------Verifica se o utilizador pediu para activar/desactivar o sensor--------//
	event message_t* Receive.receive(message_t* msg, void* payload, uint8_t len){

		if (len == sizeof(RadioMsg)) {
			RadioMsg * rmsg;
			 rmsg = (RadioMsg*)payload;
				
				if(rmsg->base_station == TRUE && rmsg->node_id == BUZZER_ID){
					call Leds.led1Toggle();
					G_enabled = rmsg->enabled;
					G_buzzer_enabled = rmsg->buzzer_enabled;
					G_propagate_buzzer = rmsg->propagation_effect_enabled;
					
					if((rmsg->buzzer_off && ringing) || (!G_buzzer_enabled) || (!G_enabled)){
						call Sounder.beepOff();
						//call Leds.led1Toggle();
						ringing = FALSE;
					}
					
					switch(G_buzzer_enabled){
						case (TRUE): call Leds.led0On(); break;
						case (FALSE): call Leds.led0Off(); break;
					}
					switch(G_propagate_buzzer){
						case (TRUE): call Leds.led2On(); break;
						case (FALSE): call Leds.led2Off(); break;
					}
				}
				if(G_enabled && G_buzzer_enabled && !ringing && rmsg->motion_detected && !rmsg->base_station) {
								call Leds.led2Toggle();
								call Sounder.beep(1000*60);
								ringing = TRUE;
					
					//call Leds.led1Toggle();	
				}

			  
		}
				if(G_enabled==TRUE){
			
		switch(G_buzzer_enabled){
			case (TRUE): call Leds.led0On();break;
			case (FALSE): call Leds.led0Off();break;
		}
	}else{call Leds.led0Off();}
    return msg;
  }



  event void AMSend.sendDone(message_t* msg, error_t err) {
    if (&pkt == msg) {
      busy = FALSE;
    }
  }
}

