package BACnetCore.CoreClasses;

/**************************************************************************
*                           MIT License
* 
* Copyright (C) 2014 Morten Kvistgaard <mk@pch-engineering.dk>
* Copyright (C) 2015 Frederic Chaxel <fchaxel@free.fr>
*
* Permission is hereby granted, free of charge, to any person obtaining
* a copy of this software and associated documentation files (the
* "Software"), to deal in the Software without restriction, including
* without limitation the rights to use, copy, modify, merge, publish,
* distribute, sublicense, and/or sell copies of the Software, and to
* permit persons to whom the Software is furnished to do so, subject to
* the following conditions:
*
* The above copyright notice and this permission notice shall be included
* in all copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
* EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
* IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
* CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
* TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
* SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
*********************************************************************/
public class BacnetAddress : IASN1encode
        {
public UInt16 net;
public byte[] adr;
public byte[] VMac = new byte[3]; // for IP V6, could be integrated also as 3 additional bytes in adr
public BacnetAddressTypes type;

// Modif FC
public BacnetAddress RoutedSource = null;

public BacnetAddress(BacnetAddressTypes type, UInt16 net, byte[] adr)
        {
        this.type = type;
        this.net = net;
        this.adr = adr;
        if (this.adr == null) this.adr = new byte[0];
        }

public BacnetAddress(BacnetAddressTypes type, String s)
        {
        this.type = type;
        switch (type)
        {
        case BacnetAddressTypes.IP:
        try
        {
        String[] IpStrCut = s.Split(':');
        IPAddress ip;
        bool IsIP = IPAddress.TryParse(IpStrCut[0], out ip);
        uint Port = Convert.ToUInt16(IpStrCut[1]);
        if (IsIP == true)
        {
        String[] Cut = IpStrCut[0].Split('.');
        adr = new byte[6];
        for (int i = 0; i < 4; i++)
        adr[i] = Convert.ToByte(Cut[i]);
        adr[4] = (byte)((Port & 0xff00) >> 8);
        adr[5] = (byte)(Port & 0xff);
        }
        }
        catch { throw new Exception(); }
        break;
        case BacnetAddressTypes.Ethernet:
        try
        {
        String[] EthStrCut = s.Split('-');
        adr = new byte[6];
        for (int i = 0; i < 6; i++)
        adr[i] = Convert.ToByte(EthStrCut[i], 16);
        }
        catch { throw new Exception(); }
        break;
        }
        }
public BacnetAddress()
        {
        type = BacnetAddressTypes.None;
        }

public override int GetHashCode()
        {
        return adr.GetHashCode();
        }
public override string ToString()
        {
        return ToString(this.type);
        }
public string ToString(BacnetAddressTypes type)
        {
        switch (type)
        {
        case BacnetAddressTypes.IP:
        if (adr == null || adr.Length < 6) return "0.0.0.0";
        return adr[0] + "." + adr[1] + "." + adr[2] + "." + adr[3] + ":" + ((adr[4] << 8) | (adr[5] << 0));
        case BacnetAddressTypes.MSTP:
        if (adr == null || adr.Length < 1) return "-1";
        return adr[0].ToString();
        case BacnetAddressTypes.PTP:
        return "x";
        case BacnetAddressTypes.Ethernet:
        StringBuilder sb1 = new StringBuilder();
        for (int i = 0; i < 6; i++)
        {
        sb1.Append(adr[i].ToString("X2"));
        if (i != 5) sb1.Append('-');
        }

        return sb1.ToString();
        case BacnetAddressTypes.IPV6:
        if (adr == null || adr.Length != 18) return "[::]";
        ushort port = (ushort)((adr[16] << 8) | (adr[17] << 0));
        byte[] Ipv6 = new byte[16];
        Array.Copy(adr, Ipv6, 16);
        IPEndPoint ep = new System.Net.IPEndPoint(new IPAddress(Ipv6), (int)port);
        return ep.ToString();

default: // Routed @ are always like this, NPDU do not contains the MAC type, only the lenght
        if (adr == null) return "?";

        if (adr.Length == 6) // certainly IP, but not sure (Newron System send it for internal usage with 4*0 bytes)
        return ToString(BacnetAddressTypes.IP);

        if (adr.Length == 18)   // Not sure it could appears, since NPDU may contains Vmac ?
        return ToString(BacnetAddressTypes.IPV6);

        if (adr.Length == 3)
        return "IPv6 VMac : " + ((int)(adr[0] << 16) | (adr[1] << 8) | adr[2]).ToString();

        StringBuilder sb2 = new StringBuilder();
        for (int i = 0; i < adr.Length; i++)
        sb2.Append(adr[i] + " ");
        return sb2.ToString();
        }
        }

public String ToString(bool SourceOnly)
        {
        if (this.RoutedSource == null)
        return ToString();
        if (SourceOnly)
        return this.RoutedSource.ToString();
        else
        return this.RoutedSource.ToString() + " via " + ToString();
        }

public override bool Equals(object obj)
        {
        if (!(obj is BacnetAddress)) return false;
        BacnetAddress d = (BacnetAddress)obj;
        if (adr == null && d.adr == null) return true;
        else if (adr == null || d.adr == null) return false;
        else if (adr.Length != d.adr.Length) return false;
        else
        {
        for (int i = 0; i < adr.Length; i++)
        if (adr[i] != d.adr[i]) return false;

        // Modif FC
        if ((RoutedSource == null) && (d.RoutedSource != null))
        return false;
        if ((d.RoutedSource == null) && (RoutedSource == null)) return true;
        return RoutedSource.Equals(d.RoutedSource);

        }

        }

// checked if device is routed by curent equipement
public bool IsMyRouter(BacnetAddress device)
        {
        if ((device.RoutedSource == null) || (RoutedSource != null))
        return false;
        if (adr.Length != device.adr.Length) return false;

        for (int i = 0; i < adr.Length; i++)
        if (adr[i] != device.adr[i]) return false;

        return true;
        }

public void ASN1encode(EncodeBuffer buffer)
        {
        ASN1.encode_opening_tag(buffer, 1);
        ASN1.encode_application_unsigned(buffer, net);
        ASN1.encode_application_octet_string(buffer, adr, 0, adr.Length);
        ASN1.encode_closing_tag(buffer, 1);
        }

public string FullHashString()
        {
        StringBuilder s = new StringBuilder(((uint)type).ToString() + "." + net.ToString() + ".");
        for (int i = 0; i < adr.Length; i++)
        s.Append(adr[i].ToString("X2"));
        if (RoutedSource != null)
        s.Append(":" + RoutedSource.FullHashString());

        return s.ToString();

        }
        }
