import React, { useState } from 'react';
import { KeyRound, ShieldAlert, CheckCircle, XCircle, Eye, EyeOff, Lock } from 'lucide-react';
import { toast } from 'react-hot-toast';
import pharmacyService from '../utils/pharmacyService';

export default function ResetPassword() {
  const [formData, setFormData] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  });
  const [showCurrent, setShowCurrent] = useState(false);
  const [showNew, setShowNew] = useState(false);
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);

  const handleChange = (e) => {
    setFormData(prev => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const getStrength = (pw) => {
    let score = 0;
    const checks = {
      length: pw.length >= 12,
      upper: /[A-Z]/.test(pw),
      lower: /[a-z]/.test(pw),
      number: /[0-9]/.test(pw),
      special: /[^A-Za-z0-9]/.test(pw)
    };
    
    if (checks.length) score += 1;
    if (checks.upper) score += 1;
    if (checks.lower) score += 1;
    if (checks.number) score += 1;
    if (checks.special) score += 1;

    return { score, checks };
  };

  const { score, checks } = getStrength(formData.newPassword);
  const strengthLabels = ['Very Weak', 'Weak', 'Fair', 'Strong', 'Very Strong'];
  const colors = ['bg-red-500', 'bg-orange-500', 'bg-yellow-500', 'bg-green-500', 'bg-emerald-600'];

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.currentPassword) {
      toast.error('Current password is required');
      return;
    }
    if (formData.newPassword.length < 8) {
      toast.error('New password must be at least 8 characters');
      return;
    }
    if (formData.newPassword === formData.currentPassword) {
      toast.error('New password must be different from current');
      return;
    }
    if (formData.newPassword !== formData.confirmPassword) {
      toast.error('Passwords do not match');
      return;
    }

    setLoading(true);
    try {
      if (pharmacyService.api) {
        await pharmacyService.api.post('/auth/change-password', {
          currentPassword: formData.currentPassword,
          newPassword: formData.newPassword
        });
      }
      toast.success('Password updated successfully');
      setSuccess(true);
      setFormData({ currentPassword: '', newPassword: '', confirmPassword: '' });
    } catch (error) {
      toast.error(error?.response?.data?.message || 'Failed to update password');
    } finally {
      setLoading(false);
    }
  };

  if (success) {
    return (
      <div className="max-w-xl mx-auto mt-10">
        <div className="bg-white p-8 rounded-2xl shadow-sm border border-emerald-100 text-center flex flex-col items-center">
          <div className="w-16 h-16 bg-emerald-100 text-emerald-600 rounded-full flex items-center justify-center mb-6">
            <CheckCircle className="w-8 h-8" />
          </div>
          <h2 className="text-2xl font-bold text-gray-900 mb-2">Password Updated!</h2>
          <p className="text-gray-500 mb-8">Your account password has been changed successfully. You can now use your new password for future logins.</p>
          <button 
            onClick={() => setSuccess(false)}
            className="px-6 py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl font-bold transition-all"
          >
            Manage Settings
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-2xl mx-auto space-y-6 pb-10">
      <div className="flex flex-col gap-1">
        <h2 className="text-2xl font-bold tracking-tight text-gray-900">Reset Password</h2>
        <p className="text-sm text-gray-500 font-medium">Update your credentials and improve your account security</p>
      </div>

      <div className="bg-amber-50 border border-amber-200 p-4 rounded-xl flex items-start gap-3">
        <ShieldAlert className="w-5 h-5 text-amber-600 shrink-0 mt-0.5" />
        <div>
          <h4 className="text-sm font-bold text-amber-800">Security Best Practices</h4>
          <p className="text-xs text-amber-700 mt-1">
            Ensure your account uses a long, random password. Avoid using dictionary words, dates, or sequential numbers. We recommend a passphrase or a password manager.
          </p>
        </div>
      </div>

      <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
        <div className="px-6 py-4 border-b border-gray-100 bg-gray-50/50 flex items-center gap-2">
          <KeyRound className="w-5 h-5 text-primary" />
          <h3 className="text-sm font-bold text-gray-800">Change Password</h3>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-6">
          <div className="space-y-1.5">
            <label className="text-xs font-bold text-slate-500 uppercase tracking-widest">Current Password</label>
            <div className="relative">
              <Lock className="w-4 h-4 absolute left-3.5 top-3.5 text-slate-400" />
              <input
                type={showCurrent ? "text" : "password"}
                name="currentPassword"
                value={formData.currentPassword}
                onChange={handleChange}
                className="w-full pl-10 pr-10 py-3 rounded-xl border border-slate-200 outline-none focus:ring-2 focus:ring-primary/20 bg-white"
                placeholder="Enter current password"
              />
              <button 
                type="button" 
                onClick={() => setShowCurrent(!showCurrent)}
                className="absolute right-3.5 top-3.5 text-slate-400 hover:text-slate-600"
              >
                {showCurrent ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
              </button>
            </div>
          </div>

          <div className="space-y-4 pt-2">
            <div className="space-y-1.5">
              <label className="text-xs font-bold text-slate-500 uppercase tracking-widest">New Password</label>
              <div className="relative">
                <Lock className="w-4 h-4 absolute left-3.5 top-3.5 text-slate-400" />
                <input
                  type={showNew ? "text" : "password"}
                  name="newPassword"
                  value={formData.newPassword}
                  onChange={handleChange}
                  className="w-full pl-10 pr-10 py-3 rounded-xl border border-slate-200 outline-none focus:ring-2 focus:ring-primary/20 bg-white"
                  placeholder="Enter new strong password"
                />
                <button 
                  type="button" 
                  onClick={() => setShowNew(!showNew)}
                  className="absolute right-3.5 top-3.5 text-slate-400 hover:text-slate-600"
                >
                  {showNew ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                </button>
              </div>
            </div>

            {/* Password Strength Meter */}
            {formData.newPassword.length > 0 && (
              <div className="bg-slate-50 p-4 rounded-xl border border-slate-100 space-y-3">
                <div className="flex justify-between items-center text-xs font-bold">
                  <span className="text-slate-600">Password Strength</span>
                  <span className={`${score > 0 ? colors[score - 1].replace('bg-', 'text-') : 'text-red-500'}`}>
                    {score === 0 ? 'Very Weak' : strengthLabels[score - 1]}
                  </span>
                </div>
                <div className="flex gap-1 h-1.5">
                  {[1, 2, 3, 4, 5].map(i => (
                    <div 
                      key={i} 
                      className={`flex-1 rounded-full ${score >= i ? colors[score - 1] : 'bg-slate-200'}`} 
                    />
                  ))}
                </div>
                <div className="grid grid-cols-2 gap-2 text-xs pt-1">
                  <div className={`flex items-center gap-1.5 ${checks.length ? 'text-emerald-600 font-medium' : 'text-slate-400'}`}>
                    {checks.length ? <CheckCircle className="w-3 h-3" /> : <XCircle className="w-3 h-3" />}
                    <span>12+ Characters</span>
                  </div>
                  <div className={`flex items-center gap-1.5 ${checks.upper ? 'text-emerald-600 font-medium' : 'text-slate-400'}`}>
                    {checks.upper ? <CheckCircle className="w-3 h-3" /> : <XCircle className="w-3 h-3" />}
                    <span>Uppercase Letter</span>
                  </div>
                  <div className={`flex items-center gap-1.5 ${checks.lower ? 'text-emerald-600 font-medium' : 'text-slate-400'}`}>
                    {checks.lower ? <CheckCircle className="w-3 h-3" /> : <XCircle className="w-3 h-3" />}
                    <span>Lowercase Letter</span>
                  </div>
                  <div className={`flex items-center gap-1.5 ${checks.number ? 'text-emerald-600 font-medium' : 'text-slate-400'}`}>
                    {checks.number ? <CheckCircle className="w-3 h-3" /> : <XCircle className="w-3 h-3" />}
                    <span>Number</span>
                  </div>
                  <div className={`flex items-center gap-1.5 ${checks.special ? 'text-emerald-600 font-medium' : 'text-slate-400'}`}>
                    {checks.special ? <CheckCircle className="w-3 h-3" /> : <XCircle className="w-3 h-3" />}
                    <span>Special Character</span>
                  </div>
                </div>
              </div>
            )}
          </div>

          <div className="space-y-1.5 pt-2">
            <label className="text-xs font-bold text-slate-500 uppercase tracking-widest">Confirm New Password</label>
            <div className="relative">
              <Lock className="w-4 h-4 absolute left-3.5 top-3.5 text-slate-400" />
              <input
                type="password"
                name="confirmPassword"
                value={formData.confirmPassword}
                onChange={handleChange}
                className="w-full pl-10 pr-10 py-3 rounded-xl border border-slate-200 outline-none focus:ring-2 focus:ring-primary/20 bg-white"
                placeholder="Confirm new password"
              />
              {formData.confirmPassword && (
                <div className="absolute right-3.5 top-3.5">
                  {formData.newPassword === formData.confirmPassword ? (
                    <CheckCircle className="w-4 h-4 text-emerald-500" />
                  ) : (
                    <XCircle className="w-4 h-4 text-red-500" />
                  )}
                </div>
              )}
            </div>
            {formData.confirmPassword && formData.newPassword !== formData.confirmPassword && (
              <p className="text-xs text-red-500 font-bold mt-1">Passwords do not match.</p>
            )}
          </div>

          <div className="pt-4 flex justify-end">
            <button
              type="submit"
              disabled={loading || !formData.currentPassword || formData.newPassword.length < 8 || formData.newPassword !== formData.confirmPassword}
              className="px-8 py-2.5 bg-primary text-white rounded-xl text-sm font-bold shadow-lg shadow-primary/20 hover:bg-blue-700 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
            >
              Update Password
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
