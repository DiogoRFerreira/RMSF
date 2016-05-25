/*
 *
 * Diogo Ferreira e José Dias
 *
 */

#include "RadioProtocolMessage.h"

configuration BinarySensorAppC{
}
implementation {
  components MainC;
  components BinarySensorNC as App;
  components LedsC;
  components BinarySensorC;

  components ActiveMessageC;
  components new AMSenderC(AM_RADIOPROTOCOLMESSAGE);
  components new AMReceiverC(AM_RADIOPROTOCOLMESSAGE);
//-------------------------------------------------------
  App.Boot -> MainC;
  App.Leds -> LedsC;
  App.BinarySensor -> BinarySensorC.BinarySensor;

  App.Packet -> AMSenderC;
  App.AMPacket -> AMSenderC;
  App.AMControl -> ActiveMessageC;
  App.AMSend -> AMSenderC;
  
    App.Receive -> AMReceiverC;
  
  
}
