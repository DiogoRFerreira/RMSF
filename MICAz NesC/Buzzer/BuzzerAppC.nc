

configuration BuzzerAppC {}

implementation
{
  components MainC, LedsC;
  components BuzzerC as App;
  components MicDeviceP as MicrophoneC;
	
  components ActiveMessageC;
  components new AMSenderC(AM_RADIOPROTOCOLMESSAGE);
  components new AMReceiverC(AM_RADIOPROTOCOLMESSAGE);
    components SounderC;

//-------------------------------------------------------
  App.Boot -> MainC;
  App.Leds -> LedsC;
	
  App.Packet -> AMSenderC;
  App.AMPacket -> AMSenderC;
  App.AMControl -> ActiveMessageC;
  App.AMSend -> AMSenderC;
    App.Receive -> AMReceiverC;
      App.Sounder -> SounderC.Mts300Sounder;
}

