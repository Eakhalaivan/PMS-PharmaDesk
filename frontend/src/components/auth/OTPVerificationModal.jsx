import React, { useState } from 'react';
import { Shield, Key, RefreshCcw, Check, X } from 'lucide-react';
import { toast } from 'react-hot-toast';
import pharmacyService from '../../utils/pharmacyService';

export default function OTPVerificationModal({ isOpen, onClose, onVerifySuccess }) {
  const [email, setEmail] = useState('');
  const [otpSent, setOtpSent] = useState(false);
  const [code, setCode] = useState('');
  const [loading, setLoading] = useState(false);

  if (!isOpen) return null;

  const handleSendOtp = async () => {
    if (!email) {
      toast.error("Please enter email address");
      return;
    }
    setLoading(true);
    try {
      await pharmacyService.sendOtp(email);
      setOtpSent(true);
      toast.success("OTP sent to your email!");
    } catch (err) {
      toast.error(err.response?.data?.message || "Failed to send OTP");
    } finally {
      setLoading(false);
    }
  };

  const handleVerifyOtp = async () => {
    if (!code) {
      toast.error("Please enter the 6-digit code");
      return;
    }
    setLoading(true);
    try {
      await pharmacyService.verifyOtp(email, code);
      toast.success("Verification successful!");
      onVerifySuccess();
      onClose();
    } catch (err) {
      toast.error("Verification failed: " + (err.response?.data?.message || "Invalid code"));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-center justify-center p-4">
      <div className="bg-white border border-gray-200 w-full max-w-md p-6 relative">
        <button onClick={onClose} className="absolute top-4 right-4 text-gray-400 hover:text-gray-600">
          <X className="w-5 h-5" />
        </button>

        <div className="flex flex-col items-center text-center">
          <div className="w-12 h-12 bg-purple-50 flex items-center justify-center text-purple-700 mb-4 border border-purple-100">
            <Shield className="w-6 h-6" />
          </div>
          <h3 className="text-lg font-medium text-gray-900 mb-1">Compliance 2FA Verification</h3>
          <p className="text-xs text-gray-500 mb-6">Enter your registered email address to receive a one-time passcode (OTP) for access.</p>
        </div>

        <div className="space-y-4">
          <div>
            <label className="block text-xs font-medium text-gray-700 mb-1">Registered Email</label>
            <div className="flex gap-2">
              <input
                type="email"
                placeholder="staff@hospital.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                disabled={otpSent}
                className="flex-1 text-sm border border-gray-300 p-2 focus:ring-2 focus:ring-purple-500 outline-none"
              />
              {!otpSent && (
                <button
                  onClick={handleSendOtp}
                  disabled={loading}
                  className="bg-purple-700 text-white px-4 py-2 text-xs font-medium hover:bg-purple-800 disabled:opacity-50"
                >
                  {loading ? <RefreshCcw className="w-4 h-4 animate-spin" /> : "Send OTP"}
                </button>
              )}
            </div>
          </div>

          {otpSent && (
            <div>
              <label className="block text-xs font-medium text-gray-700 mb-1">One-Time Passcode</label>
              <input
                type="text"
                placeholder="Enter 6-digit OTP"
                maxLength={6}
                value={code}
                onChange={(e) => setCode(e.target.value)}
                className="w-full text-center text-lg tracking-widest font-bold border border-gray-300 p-2 focus:ring-2 focus:ring-purple-500 outline-none"
              />
              <button
                onClick={handleVerifyOtp}
                disabled={loading}
                className="w-full mt-4 bg-purple-700 text-white py-2 text-sm font-medium hover:bg-purple-800 flex items-center justify-center gap-2 disabled:opacity-50"
              >
                {loading ? <RefreshCcw className="w-4 h-4 animate-spin" /> : <Check className="w-4 h-4" />}
                Verify Code
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
