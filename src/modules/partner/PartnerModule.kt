package modules.partner

import core.Module
import modules.partner.partners.PartnersRoute
import modules.partner.referrals.ReferralRoute

object PartnerModule : Module("Partner", PartnersRoute(), ReferralRoute())