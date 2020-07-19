package org.appling.rallyx.miro;

/*
 "status" : 400,
  "code" : "lockedWidget",
  "message" : "Trying to change locked widget",
  "context" : null,
  "type" : "error"
}
 */
public class ErrorResponse
{
   public int status;
   public String code;
   public String message;
   // context can be different objects.  GSON needs matching object types
   //public String context;
   public String type;
}
